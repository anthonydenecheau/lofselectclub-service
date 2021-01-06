package com.scc.lofselectclub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scc.lofselectclub.exceptions.EntityNotFoundException;
import com.scc.lofselectclub.config.ServiceConfig;
import com.scc.lofselectclub.model.BreederStatistics;
import com.scc.lofselectclub.model.SerieDefinition;
import com.scc.lofselectclub.model.GenericStatistics;
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.breeder.BreederMonthStatistics;
import com.scc.lofselectclub.template.breeder.BreederAffixRank;
import com.scc.lofselectclub.template.breeder.BreederAffixStatistics;
import com.scc.lofselectclub.template.breeder.BreederAffixVariety;
import com.scc.lofselectclub.template.breeder.BreederBreed;
import com.scc.lofselectclub.template.breeder.BreederResponseObject;
import com.scc.lofselectclub.template.breeder.BreederBreedStatistics;
import com.scc.lofselectclub.template.breeder.BreederVarietyStatistics;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.breeder.BreederVariety;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class BreederService extends AbstractGenericService<BreederResponseObject,BreederStatistics> {

   public BreederService() {
      super();
      this.setGenericTemplate(new BreederResponseObject());
      this.setType(BreederStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(BreederService.class);

   @Autowired
   protected BreederRepository breederRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;
   
   private final List<BreederStatistics> _emptyBreederStatistics = new ArrayList<BreederStatistics>();

   private Integer minVal = 0;
   private Integer maxVal = 0;
   private int limitTopN = 0;
   
   private List<BreederAffixRank> allTopNBreed = new ArrayList<BreederAffixRank>();
   private HashMap<TupleVariety,List<BreederAffixRank>> allTopNVariety = new HashMap<TupleVariety,List<BreederAffixRank>>();
   
   /**
    * Retourne les données statistiques liées à l'élevage pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>BreederResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackBreederList"
         , threadPoolKey = "getStatisticsBreeder"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public BreederResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the breederService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

      // topN affixe
      this.limitTopN = config.getLimitTopNAffix();

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // Lecture des races associées au club pour lesquelles des données ont été calculées
         List<BreederBreed> _breeds = populateBreeds(idClub);
         
         // Réponse
         return getGenericTemplate()
               .withBreeds(_breeds)
               .withSize(_breeds.size());

      } finally {
         newSpan.tag("peer.service", "postgres");
         newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
         tracer.close(newSpan);
      }
   }

   /**
    * Fonction fallbackMethod de la fonction principale <code>getStatistics</code>
    * (Hystrix Latency / Fault Tolerance)
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>BreederResponseObject</code>
    */
   private BreederResponseObject buildFallbackBreederList(int idClub) {

      List<BreederBreed> list = new ArrayList<BreederBreed>();
      list.add(new BreederBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
      
   }

   /**
    * Retourne le nombre d'éleveurs ayant produit sur chacune des séries
    * 
    * @param _plages Liste des séries ou plages définies pour la race lue (ex: 2 à 4 portées)
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>series</code> des objets <code>BreederBreedStatistics</code>, <code>BreederMonthStatistics</code> et <code>BreederVarietyStatistics</code>
    */
   private List<Map<String, Object>> extractSeries(List<SerieDefinition> _plages, List<BreederStatistics> _list) {

      List<Map<String, Object>> _series = new ArrayList<Map<String, Object>>();

      try {
         // Par tranche
         for (SerieDefinition plage : _plages) {
   
            Map<String, Object> _serie = new HashMap<String, Object>();
   
            minVal = (plage.getMinValue() == null ? 0 : plage.getMinValue());
            maxVal = (plage.getMaxValue() == null ? 0 : plage.getMaxValue());
   
            Set<Integer> units = _list
                  .stream()
                  .collect(Collectors.collectingAndThen(
                        Collectors.groupingBy(BreederStatistics::getIdEleveur, Collectors.counting()),
                        (map) -> map.entrySet().stream().filter(e -> matchesRange(e, minVal, maxVal)).map(e -> e.getKey())
                              .collect(Collectors.toSet())));
   
            _serie.put("serie", plage.getLibelle());
            _serie.put("qtity", units.size());
            _series.add(new HashMap<String, Object>(_serie));
   
         }
      } catch (Exception e) {
         logger.error("extractSeries : {}",e.getMessage());
      } finally {
      } 
      
      return _series;
   }

   /**
    * Retourne le classement des affixes s/ la variété ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>affixes</code> de l'objet <code>BreederAffixStatistics</code>
    */
   private List<BreederAffixRank> extractTopNVarietyOverYear(int _year, List<BreederStatistics> _list) {

      
      List<BreederAffixRank> _topsN = new ArrayList<BreederAffixRank>();
      int position = 0;
      int qtityExaequo = 0;
      int currentPosition = 0;
      
      try {

         // On extrait les affixes du topN varietes
         List<BreederAffixRank> _listAffixVarietyTopN = this.allTopNVariety.entrySet()
               .stream()
               .filter(r -> r.getKey().getId() == this._idVariety)
               .flatMap(e -> e.getValue().stream())
               .collect(Collectors.toList())
         ;
         
         // On ne sélectionne que les affixes contenus dans allTopNVariety
         // On groupe les affixes par qtites pour la variété et l'année en cours
         Map<String, Long> _affixes = _list
               .stream()
               .filter(StreamUtils.onlyTopNAffixe(_listAffixVarietyTopN))
               .filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getAffixeEleveur, Collectors.counting()));

         // On sélectionne uniquement les affixes du top20 pour la variété et l'année en cours
         Set<BreederAffixRank> _top20BreederAffix = _listAffixVarietyTopN
               .stream()
               .filter(x -> _year == x.getYear())
               .collect(Collectors.toSet())
         ;
         
         // On trie la liste des affixes du topN pour la variété et l'année en cours
         Map<String, Long> result = _affixes.entrySet().stream()
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (oldValue, newValue) -> oldValue, LinkedHashMap::new));
         
         // On précise les positions
         for (Entry<String, Long> _affixe : result.entrySet()) {

            // Si l'affixe n'est pas présent dans le top20, appartient-il au topN ?
            // si oui, il est sorti du classement pour l'année en cours; si non il n'est pas pris en compte
            // on conserve l'information qtités
            // On n'applique pas ce filtre pour la dernière année
            if (_year != this._lastYear && !isTop20(_affixe.getKey(), _top20BreederAffix)) {
               if (isTopNVariety(_affixe.getKey(), _listAffixVarietyTopN)) {
                  _topsN.add(
                        new BreederAffixRank()
                        .withPosition(0)
                        .withName(_affixe.getKey())
                        .withQtity((int) (long) _affixe.getValue())
                     );
               }
               continue;
            }            

            if (qtityExaequo == (int) (long)_affixe.getValue() ) {
               position++;
            } else
               currentPosition = ++position;
            
            qtityExaequo = (int) (long)_affixe.getValue();
            _topsN.add(
               new BreederAffixRank()
                  .withPosition(currentPosition)
                  .withName(_affixe.getKey())
                  .withQtity(qtityExaequo)
            );
            
         }

         // On complète la liste des affixes du topN manquants (attention, topN contient des doublons (prend en charge les années)
         Set<String> _affixesTopN = _listAffixVarietyTopN
               .stream()
               .map(p -> p.getName())
               .distinct()
               .collect(Collectors.toSet());
         
         for (String _affix : _affixesTopN) {
            if (!isPresent(_affix, _topsN))
               _topsN.add(
                     new BreederAffixRank()
                        .withPosition(0)
                        .withName(_affix)
                        .withQtity(0)
                  );
         }

      } catch (Exception e) {
         logger.error("extractTopNVarietyOverYear {}", e.getMessage());
      }

      _topsN.sort(Collections.reverseOrder(Comparator.comparing(BreederAffixRank::getQtity)));
      
      return _topsN;

   }
   
   /**
    * Retourne le classement des affixes s/ la race ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>affixes</code> de l'objet <code>BreederAffixStatistics</code>
    */
   private List<BreederAffixRank> extractTopNBreedOverYear(int _year, List<BreederStatistics> _list) {

      List<BreederAffixRank> _topsN = new ArrayList<BreederAffixRank>();
      int position = 0;
      int qtityExaequo = 0;
      int currentPosition = 0;
      
      try {

         // On ne sélectionne que les affixes contenus dans allTopNBreed
         // On groupe les affixes par qtites pour l'année en cours
         Map<String, Long> _affixes = _list
               .stream()
               .filter(StreamUtils.onlyTopNAffixe(this.allTopNBreed))
               .filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getAffixeEleveur, Collectors.counting()));
         
         // On sélectionne uniquement les affixes du top20 pour l'année en cours
         Set<BreederAffixRank> _top20BreederAffix = this.allTopNBreed
               .stream()
               .filter(x -> _year == x.getYear())
               .collect(Collectors.toSet())
         ;

         // On trie la liste des affixes du topN pour l'année en cours
         Map<String, Long> result = _affixes.entrySet().stream()
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        
         // On précise les positions
         for (Entry<String, Long> _affixe : result.entrySet()) {

            // Si l'affixe n'est pas présent dans le top20, appartient-il au topN ?
            // si oui, il est sorti du classement pour l'année en cours; si non il n'est pas pris en compte
            // on conserve l'information qtités
            // On n'applique pas ce filtre pour la dernière année
            if (_year != this._lastYear && !isTop20(_affixe.getKey(), _top20BreederAffix)) {
               if (isTopN(_affixe.getKey())) {
                  _topsN.add(
                        new BreederAffixRank()
                        .withPosition(0)
                        .withName(_affixe.getKey())
                        .withQtity((int) (long) _affixe.getValue())
                     );
               }
               continue;
            }            

            if (qtityExaequo == (int) (long)_affixe.getValue() ) {
               position++;
            } else
               currentPosition = ++position;
            
            qtityExaequo = (int) (long)_affixe.getValue();
            _topsN.add(
               new BreederAffixRank()
                  .withPosition(currentPosition)
                  .withName(_affixe.getKey())
                  .withQtity(qtityExaequo)
            );
            
         }
         
         // On complète la liste des affixes du topN manquants (attention, topN contient des doublons (prend en charge les années)
         Set<String> _affixesTopN = this.allTopNBreed.stream().map(p -> p.getName()).distinct().collect(Collectors.toSet());
         for (String _affix : _affixesTopN) {
            if (!isPresent(_affix, _topsN))
               _topsN.add(
                     new BreederAffixRank()
                        .withPosition(0)
                        .withName(_affix)
                        .withQtity(0)
                  );
         }
         
      } catch (Exception e) {
         logger.error("extractTopNBreedOverYear {}", e.getMessage());
      } finally {
         // On trie les résultats par quantites décroissante
         _topsN.sort(Collections.reverseOrder(Comparator.comparing(BreederAffixRank::getQtity)));
      }

      return _topsN;

   }

   private boolean isTop20(String _affixe, Set<BreederAffixRank> _top20) {
      
      // L'affixe est-il présent dans le top20
      BreederAffixRank x = _top20.stream()
            .filter(p -> p.getName().equals(_affixe))
            .findAny()
            .orElse(null);
      
      if (x == null)
         return false;
      else 
         return true;
   }

   private boolean isTopN(String _name) {
      
      List<BreederAffixRank> _affix = Collections.singletonList(new BreederAffixRank().withName(_name));
      BreederAffixRank y = this.allTopNBreed.stream()
            .filter(StreamUtils.isTopNAffixe(_affix))
            .findAny()
            .orElse(null);
      if (y == null)
         return false;
      else
         return true;

   }
   
   private boolean isTopNVariety(String _name, List<BreederAffixRank> _listAffixVarietyTopN) {
      
      List<BreederAffixRank> _affix = Collections.singletonList(new BreederAffixRank().withName(_name));
      BreederAffixRank y = _listAffixVarietyTopN.stream()
            .filter(StreamUtils.isTopNAffixe(_affix))
            .findAny()
            .orElse(null);
      if (y == null)
         return false;
      else
         return true;

   }
   
   private boolean isPresent(String _affixe, List<BreederAffixRank> _top) {
      
      BreederAffixRank x = _top.stream()
            .filter(p -> p.getName().equals(_affixe))
            .findAny()
            .orElse(null);
      
      if (x == null)
         return false;
      else 
         return true;
   }
   
   /**
    * Détermine si la production de l'éleveur est inclus dans la série lue
    * 
    * @param e          Nombre de portées pour un éleveur
    * @param range_min  Limite inférieure de la série
    * @param range_max  Limite supérieure de la série
    * @return           <code>true</code> si l'éleveur appartient à la série
    */
   private boolean matchesRange(Entry<Integer, Long> e, int range_min, int range_max) {

      if (range_min > 0 && range_max == 0)
         return (e.getValue() == range_min);
      if (range_min > 0 && range_max > 0)
         return (e.getValue() >= range_min && e.getValue() <= range_max);
      if (range_min == 0 && range_max > 0)
         return (e.getValue() > range_max);
      return false;
   }

   /**
    * Retourne le nombre d'éleveurs
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>qtity</code> de l'objet <code>BreederBreedStatistics</code> et <code>BreederVarietyStatistics</code>
    */
   private int sumBreeders(List<BreederStatistics> _list) {

       Map<Integer, Long> _breeder = _list.stream()
             .collect(Collectors.groupingBy(BreederStatistics::getIdEleveur,
                   Collectors.counting()));
       ;
      
       return _breeder.size();

   }

   /**
    * Construction de 2 listes des affixes qui ont le plus produit sur les 5 dernières années (liste race, liste variété)
    * 
    * @param _minYear   Année plancher
    * @param _list      Liste des données de production à analyser
    * @return           Liste des affixes avec le nombre de portées
    */
   private void extractTopAffixes(int _minYear, List<BreederStatistics> _list) {

      List<BreederAffixRank> _sortedAffixesTopN = new ArrayList<BreederAffixRank>();
      HashMap<TupleVariety,List<BreederAffixRank>> _sortedAffixesTopNByVariety = new HashMap<TupleVariety,List<BreederAffixRank>>();
      Set<BreederAffixRank> _tmpAffixesTopNVariety = new HashSet<BreederAffixRank>();

      // 1 liste topN pour une race
      // n liste topN par variete
      try {
         
         // Sélection de l'année
         Map<Integer, List<BreederStatistics>> _breedGroupByYear = getYearStatistics(_list);
         for (Map.Entry<Integer, List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

            // 1. On groupe les affixes par qtites (ne prends pas en compte les affixes vides)
            Map<String, Long> _bestOfAffixesBreedOverYear = _breedOverYear.getValue()
                  .stream()
                  .filter(x -> (!"".equals(x.getAffixeEleveur()) && x.getAffixeEleveur() != null))
                  .collect(Collectors.groupingBy(BreederStatistics::getAffixeEleveur, Collectors.counting()));
   
            // 2. Classement dernière année : on enregistre les affixes ayant produit
            //    Pour les années précédantes, on ne conserve que les 20 meilleurs que l'on ajoute à notre liste existante (l'objet Set nous prémunit des doublons)
            if (_breedOverYear.getKey() == this._lastYear)
               _sortedAffixesTopN.addAll(
                     _bestOfAffixesBreedOverYear.entrySet()
                        .stream()
                        .filter(x -> x.getValue() > 0 )
                        .map(a -> new BreederAffixRank(a.getKey(), _breedOverYear.getKey(), (int) (long) a.getValue()))
                        .collect(Collectors.toList())
                     );
            else
               _sortedAffixesTopN.addAll(
                     _bestOfAffixesBreedOverYear.entrySet()
                        .stream()
                        .filter(x -> x.getValue() > 0 )
                        .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                        .limit(this.limitTopN)
                        .map(a -> new BreederAffixRank(a.getKey(), _breedOverYear.getKey(), (int) (long) a.getValue()))
                        .collect(Collectors.toList())
                     );
   
            // 3. On ajoute les topN pour chacune des variétés
            Map<TupleVariety, List<BreederStatistics>> _allVariety = getVarietyStatistics(_breedOverYear.getValue());
            for (Map.Entry<TupleVariety, List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {

               _tmpAffixesTopNVariety.clear();

               Map<String, Long> _bestOfAffixesVarietyOverYear = _currentVariety.getValue()
                     .stream()
                     .filter(x -> (!"".equals(x.getAffixeEleveur()) && x.getAffixeEleveur() != null))
                     .collect(Collectors.groupingBy(BreederStatistics::getAffixeEleveur, Collectors.counting()));

               if (_breedOverYear.getKey() == this._lastYear)
                  _tmpAffixesTopNVariety.addAll(
                        _bestOfAffixesVarietyOverYear.entrySet().stream()
                        .filter(x -> x.getValue() > 0 )
                        .map(a -> new BreederAffixRank(a.getKey(), _breedOverYear.getKey(), (int) (long)a.getValue()))
                        .collect(Collectors.toSet())
                        );
               else
                  _tmpAffixesTopNVariety.addAll(
                        _bestOfAffixesVarietyOverYear.entrySet().stream()
                        .filter(x -> x.getValue() > 0 )
                        .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                        .limit(this.limitTopN)
                        .map(a -> new BreederAffixRank(a.getKey(), _breedOverYear.getKey(), (int) (long)a.getValue()))
                        .collect(Collectors.toSet())
                        );
                  
               for (BreederAffixRank p : _tmpAffixesTopNVariety) {
                  _sortedAffixesTopNByVariety.computeIfAbsent(
                        _currentVariety.getKey()
                        , k -> new ArrayList<>()).add( 
                              new BreederAffixRank(p.getName(), p.getYear(), (int) (long)p.getQtity())
                  );
               }
               
            }
            
         }
   
         // On conserve le topN affixe (null-safe way)
         this.allTopNBreed = Optional.ofNullable(_sortedAffixesTopN)
               .map(List::stream)
               .orElseGet(Stream::empty)
               .collect(Collectors.toList());
   
         this.allTopNVariety = Optional.ofNullable(_sortedAffixesTopNByVariety)
               .map(x -> x.entrySet().stream())
               .orElseGet(Stream::empty)
               .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(oldValue, newValue) -> oldValue, LinkedHashMap::new));
                  
      } catch (Exception e) {
         logger.error("extractTopAffixes : {}",e.getMessage());
      } finally {
      } 
      
   }
   
   /**
    * Retourne le classement des affixes ayant produit le plus de portée (ventilées
    * sur les variétés de la race) dans l'année
    * 
    * @param _year         Année
    * @param _stats        Référentiel des meilleurs affixes sur les 5 dernières années
    * @return              Propriété <code>variety</code> de l'objet <code>BreederAffixStatistics</code>
    */
   @SuppressWarnings("unchecked")
   private <T> T readVarietyTopN(int _year, List<T> _stats) {
      
      // Caste la liste
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      return (T) new BreederAffixVariety()
         .withId(this._idVariety)
         .withName(this._nameVariety)
         .withAffixes(extractTopNVarietyOverYear(_year, _list));
      
   }
   
   /**
    * @param _seriesDefinition   Liste des plages définies pour la race
    * @param _stats              Liste des données de production à analyser
    * @return                    Propriété <code>variety</code> de l'objet <code>BreederBreedStatistics</code>
    */
   @SuppressWarnings("unchecked")
   private <T> T readVariety(List<SerieDefinition> _seriesDefinition, List<T> _stats) {
      
      int _qtity = 0;
      BreederVarietyStatistics _varietyStatistics = null;
      
      try {
         // Caste la liste
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         // Total des éleveurs de l'année en cours
         _qtity = sumBreeders(_list);
   
         // Recherche Production pour les plages paramétrées s/ la variété en cours
         List<Map<String, Object>> _series = extractSeries(_seriesDefinition, _list);
   
         // Création de l'objet VarietyStatistics
         _varietyStatistics = new BreederVarietyStatistics()
               .withQtity(_qtity)
               .withSeries(_series);

      } catch (Exception e) {
         logger.error("readVariety : {}",e.getMessage());
      } finally {
      } 
      
      return (T) new BreederVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withStatistics(_varietyStatistics);
      
   }
   
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {
      
      // cas de l'objet topN
      if (_parameters.isTopN())
         return readVarietyTopN(_parameters.getYear(), _stats);

      return readVariety(_parameters.getSeries(), _stats);
            
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {

      // cas de l'objet topN
      if (_parameters.isTopN())
         return (T) new BreederAffixVariety()
               .withId(_variety.getId())
               .withName(_variety.getName())
               .withAffixes(fullEmptyTopNVariety(_variety.getId()));
     
      // cas de l'objet topOfTheYear
      if (_parameters.isTopOfTheYear())
         return (T) new BreederAffixVariety()
               .withId(_variety.getId())
               .withName(_variety.getName())
               .withAffixes(fullEmptyTopNVariety(_variety.getId()));

      List<Map<String, Object>> _series = extractSeries(_parameters.getSeries(), new ArrayList<BreederStatistics>());
      BreederVarietyStatistics _varietyStatistics = new BreederVarietyStatistics()
            .withQtity(0)
            .withSeries(_series);
      
      return (T) new BreederVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withStatistics(_varietyStatistics);
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M getDataStatistics(int idClub) {
      return 
            (M) breederRepository.findByIdClub(idClub, orderByTri())
            .stream()
            .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readYear(List<T> _stats, int _year) {
      
      int _qtity = 0;
      List<Map<String, Object>> _series = null;
      List<BreederVariety> _variety = null;
      List<BreederMonthStatistics> _months = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         // Total des éleveurs de l'année en cours
         _qtity = sumBreeders(_list);
         
         // Recherche Production de l'année en cours pour les plages paramétrées s/ la race
         _series = extractSeries(this._serieQtity, _list);
   
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list, new ParametersVariety(this._serieQtity));
   
         // Attention : traitement des mois pour lesquels aucune production n'a été enregistrée
         if ("MM".equals(this._period)) {
   
            int _month = 0;
            int _monthQtity = 0;
            int[] _listMonths = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
            
            _months = new ArrayList<BreederMonthStatistics>();
   
            // On complète les données mensuelles (on ajoute un tri == permet de détecter
            // les ruptures dans la production)
            Map<Integer, List<BreederStatistics>> _breedGroupByMonth = _list
                  .stream()
                  .collect(StreamUtils.sortedGroupingBy(BreederStatistics::getMois));
            for (Map.Entry<Integer, List<BreederStatistics>> _breedOverMonth : _breedGroupByMonth.entrySet()) {
   
               _month = _breedOverMonth.getKey();
   
               // Suppression du mois traité
               _listMonths = ArrayUtils.removeElement(_listMonths, _month);
   
               // Total des éleveurs du mois en cours
               _monthQtity = sumBreeders(_breedOverMonth.getValue());
   
               // Recherche Production du mois en cours pour les plages paramétrées s/ la race
               List<Map<String, Object>> _seriesMonth = extractSeries(this._serieQtity, _breedOverMonth.getValue());
   
               // Lecture des variétés s/ la race en cours (et pour le mois en cours)
               List<BreederVariety> _varietyMonth = populateVarieties(_breedOverMonth.getValue(), new ParametersVariety(this._serieQtity));
   
               BreederMonthStatistics _monthStatitics = new BreederMonthStatistics()
                     .withMonth(_month)
                     .withQtity(_monthQtity)
                     .withSeries(_seriesMonth)
                     .withVariety(_varietyMonth);
   
               _months.add(_monthStatitics);
            }
   
            // Complète les infos s/ les mois manquants
            if (_listMonths.length > 0)
               for (int i = 0; i < _listMonths.length; i++) {
                  _months.add(new BreederMonthStatistics()
                        .withMonth(_listMonths[i])
                        .withQtity(0)
                        .withSeries(extractSeries(this._serieQtity, this._emptyBreederStatistics))
                        .withVariety(populateVarieties(this._emptyBreederStatistics, new ParametersVariety(this._serieQtity))));
               }
   
            // Mise à jour == Tri s/ les mois
            _months.sort(Comparator.comparing(BreederMonthStatistics::getMonth));
         }

      } catch (Exception e) {
         logger.error("readYear : {}",e.getMessage());
      } finally {
      } 
      
      // Création de l'objet Statistique de l'année en cours pour la race (inclus variété)
      return (T) new BreederBreedStatistics()
            .withYear(_year)
            .withQtity(_qtity)
            .withSeries(_series)
            .withVariety(_variety)
            .withMonths(_months);

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new BreederBreedStatistics()
            .withYear(_year)
            .withQtity(0)
            .withSeries(extractSeries(this._serieQtity, this._emptyBreederStatistics))
            .withVariety(populateVarieties(this._emptyBreederStatistics, new ParametersVariety(this._serieQtity)))
            .withMonths(new ArrayList<BreederMonthStatistics>());
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readTopN(List<T> _stats, int _year) {
      
      List<BreederAffixRank> _topsN = null;
      List<BreederAffixVariety> _topNVariety =  null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Recherche TopN Affixe de l'année en cours s/ la race et sur les varietes
         _topsN = extractTopNBreedOverYear(_year, _list);
   
         // Lecture TopN Affixe par variétés s/ la race en cours (et pour l'année en cours)      
         _topNVariety = populateVarieties(_list, new ParametersVariety(_year,true,false));
      
      } catch (Exception e) {
         logger.error("readTopN : {}",e.getMessage());
      } finally {
      }
      
      return (T) new BreederAffixStatistics()
            .withYear(_year)
            .withAffixes(_topsN)
            .withVariety(_topNVariety);
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyTopN(int _year) {
      return (T) new BreederAffixStatistics()
            .withYear(_year)
            .withAffixes(fullEmptyTopNBreed())
            .withVariety(populateVarieties(this._emptyBreederStatistics, new ParametersVariety(_year,true,false)));
   }
   
   /**
    * Retourne la liste des topN affixe s/ la race pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */
   private List<BreederAffixRank> fullEmptyTopNBreed() {
      
      return this.allTopNBreed.stream()
            .map(p -> p.getName())
            .distinct()
            .map (s -> new BreederAffixRank(0, s, 0))
            .collect(Collectors.toList());
   }

   /**
    * Retourne la liste des topN affixe s/ la variété pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */
   private List<BreederAffixRank> fullEmptyTopNVariety(int idVariety) {

      return this.allTopNVariety.entrySet()
            .stream()
            .filter(r -> r.getKey().getId() == idVariety)
            .flatMap(e -> e.getValue().stream())
            .map(p -> p.getName())
            .distinct()
            .map(s -> new BreederAffixRank(0, s, 0))
            .collect(ArrayList::new, ArrayList::add,ArrayList::addAll);            
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readBreed(List<T> _stats) {

      List<BreederBreedStatistics> _breedStatistics = null;
      List<BreederAffixStatistics> _affixesStatistics = null;
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Lecture des plages paramétrées des quantités pour la race en cours
         setQtitySeries(this._idBreed);
   
         // Lecture des années (on ajoute un tri)
         _breedStatistics = populateYears(_list);
   
         // Recherche TopN Affixe s/ la race
         // Regle de sélection :
         // Pour chaque année, on sélectionne le top N de la race et pour chacune des varietes.
         // Ces listes vont nous servir de base pour construire le classement s/ chaque année
         setYearSeries(this._idBreed);
         extractTopAffixes(this._serieYear[0], _list);
         _affixesStatistics = populateTopN(_list);
         
      } catch (Exception e) {
         logger.error("readBreed : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Race
      return (T) new BreederBreed()
            .withId(this._idBreed)
            .withName(this._nameBreed)
            .withStatistics(_breedStatistics)
            .withTopN(_affixesStatistics)
            ;
   }

}
