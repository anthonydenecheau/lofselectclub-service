package com.scc.lofselectclub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scc.lofselectclub.config.ServiceConfig;
import com.scc.lofselectclub.exceptions.EntityNotFoundException;
import com.scc.lofselectclub.model.BreederStatistics;
import com.scc.lofselectclub.model.GenericStatistics;
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.parent.ParentVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.utils.TypeGender;
import com.scc.lofselectclub.utils.TypeRegistrationMother;
import com.scc.lofselectclub.utils.TypeRegistrationFather;
import com.scc.lofselectclub.template.parent.ParentFather;
import com.scc.lofselectclub.template.parent.ParentAffixVariety;
import com.scc.lofselectclub.template.parent.ParentBreed;
import com.scc.lofselectclub.template.parent.ParentResponseObject;
import com.scc.lofselectclub.template.parent.ParentBreedStatistics;
import com.scc.lofselectclub.template.parent.ParentCotation;
import com.scc.lofselectclub.template.parent.ParentFatherStatistics;
import com.scc.lofselectclub.template.parent.ParentFrequency;
import com.scc.lofselectclub.template.parent.ParentFrequencyDetail;
import com.scc.lofselectclub.template.parent.ParentGender;
import com.scc.lofselectclub.template.parent.ParentRegisterType;
import com.scc.lofselectclub.template.parent.ParentRegisterTypeFather;
import com.scc.lofselectclub.template.parent.ParentRegisterTypeMother;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class ParentService extends AbstractGenericService<ParentResponseObject,BreederStatistics> {

   public ParentService() {
      super();
      this.setGenericTemplate(new ParentResponseObject());
      this.setType(BreederStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(ParentService.class);

   @Autowired
   protected BreederRepository breederRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;

   private int limitTopN = 0;
   private List<ParentFather> allTopNBreed = new ArrayList<ParentFather>();
   private HashMap<TupleVariety,List<ParentFather>> allTopNVariety = new HashMap<TupleVariety,List<ParentFather>>();
   private List<ParentFather> allTopOfTheYearBreed = new ArrayList<ParentFather>();
   private HashMap<TupleVariety,List<ParentFather>> allTopOfTheYearVariety = new HashMap<TupleVariety,List<ParentFather>>();
   
   private final List<BreederStatistics> _emptyParentsStatistics = new ArrayList<BreederStatistics>();


   /**
    * Retourne les données statistiques liées aux géniteurs pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>ParentResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackParentList"
         , threadPoolKey = "getStatisticsParent"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public ParentResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the parentService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

      // topN etalon
      this.limitTopN = config.getLimitTopNFathers();

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // On parcourt les races associées au club pour lesquelles des données ont été calculées
         List<ParentBreed> _breeds = populateBreeds(idClub);

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
    * @return        Objet <code>ParentResponseObject</code>
    */
   private ParentResponseObject buildFallbackParentList(int idClub) {

      List<ParentBreed> list = new ArrayList<ParentBreed>();
      list.add(new ParentBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }

   /**
    * Retourne la répartition des étalons par type d'inscription
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>origins</code> de l'objet <code>ParentBreedStatistics</code>
    * @see TypeRegistrationMother
    */
   private ParentGender extractFather(List<BreederStatistics> _list) {

      ParentGender _male = new ParentGender();

      int _qtity = 0;
      int _qtityTypeFrancais = 0;
      int _qtityTypeImport = 0;
      int _qtityTypeEtranger = 0;

      try {

         // Male : Groupement par type d'inscription (sans doublons)
         Map<Integer, Integer> _map = _list.stream()
               .collect(Collectors.groupingBy(BreederStatistics::getTypeEtalon
                           , Collectors.collectingAndThen(
                                Collectors.mapping(BreederStatistics::getIdEtalon, Collectors.toSet()), Set::size)));

         for (Integer key : _map.keySet()) {
            Integer value = _map.get(key);

            switch (TypeRegistrationFather.fromId(key)) {
            case FRANCAIS:
               _qtityTypeFrancais += value;
               break;
            case IMPORTES:
               _qtityTypeImport += value;
               break;
            case ETRANGERS:
               _qtityTypeEtranger += value;
               break;
            default:
               break;
            }

            // total tous types confondus
            _qtity += value;
            
         }

         List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);
         
         double _percent = 0;
         double _arrondi = 0;
         
         for (TypeRegistrationFather s : TypeRegistrationFather.values()) {
            int _qtityType = 0;
            switch (s) {
               case FRANCAIS:
                  _qtityType = _qtityTypeFrancais;
                  break;
               case IMPORTES:
                  _qtityType = _qtityTypeImport;
                  break;
               case ETRANGERS:
                  _qtityType = _qtityTypeEtranger;
                  break;
               default:
                  break;
            }

            _percent = Precision.round((double) _qtityType / (double) _qtity, 4);
            _arrondi += _percent;
            
            ParentRegisterType _type = new ParentRegisterTypeFather()
                  .withRegistration(s)
                  .withQtity(_qtityType)
                  .withPercentage(format.format(_percent));
            _types.add(_type);
         }

         // Gestion des arrondis
         if (_types.size() > 1) {
            if (_arrondi != (double)1) {
               Stream<ParentRegisterType> stream = _types.stream();
               ((ParentRegisterTypeFather) stream.reduce((first, second) -> second)
                  .orElse(null))
                  .setPercentage(format.format(Precision.round(_percent,4)+Precision.round((1-_arrondi),4)));
            }
         }

         // Lecture du nb de géniteur par cotations
         List<ParentCotation> _cotations = extractCotation("M", _list);
         
         _male = _male 
               .withQtity(_qtity)
               .withRegisterType(_types)
               .withCotations(_cotations);

      } catch (Exception e) {
         logger.error("extractFather : {}",e.getMessage());
      } finally {
      }

      return _male ;
   }

   /**
    * Retourne la répartition des lices par type d'inscription
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>origins</code> de l'objet <code>ParentBreedStatistics</code>
    * @see TypeRegistrationMother
    */
   private ParentGender extractMother(List<BreederStatistics> _list) {

      ParentGender _female = new ParentGender();

      int _qtity = 0;
      int _qtityTypeFrancais = 0;
      int _qtityTypeImport = 0;

      try {

         // Femelle : Groupement par type d'inscription (sans doublons)
         Map<Integer, Integer> _map = _list.stream()
               .collect(Collectors.groupingBy(BreederStatistics::getTypeLice
                        , Collectors.collectingAndThen(
                              Collectors.mapping(BreederStatistics::getIdLice, Collectors.toSet()), Set::size)));

         for (Integer key : _map.keySet()) {
            Integer value = _map.get(key);

            switch (TypeRegistrationMother.fromId(key)) {
               case FRANCAIS:
                  _qtityTypeFrancais += value;
                  break;
               case IMPORTES:
                  _qtityTypeImport += value;
                  break;
               default:
                  break;
            }

            // total tous types confondus
            _qtity += value;
         }

         List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);
         
         double _percent = 0;
         double _arrondi = 0;
         
         for (TypeRegistrationMother s : TypeRegistrationMother.values()) {
            int _qtityType = 0;
            switch (s) {
               case FRANCAIS:
                  _qtityType = _qtityTypeFrancais;
                  break;
               case IMPORTES:
                  _qtityType = _qtityTypeImport;
                  break;
               default:
                 break;
            }

            _percent = Precision.round((double) _qtityType / (double) _qtity, 4);
            _arrondi += _percent;
            
            ParentRegisterType _type = new ParentRegisterTypeMother()
                  .withRegistration(s)
                  .withQtity(_qtityType)
                  .withPercentage(format.format(_percent));
            _types.add(_type);
         }

         // Gestion des arrondis
         if (_types.size() > 1) {
            if (_arrondi != (double)1) {
               Stream<ParentRegisterType> stream = _types.stream();
               ((ParentRegisterTypeMother) stream.reduce((first, second) -> second)
                  .orElse(null))
                  .setPercentage(format.format(Precision.round(_percent,4)+Precision.round((1-_arrondi),4)));
            }
         }
         
         // Lecture du nb de géniteur par cotations
         List<ParentCotation> _cotations = extractCotation("F", _list);
         
         _female = _female
               .withQtity(_qtity)
               .withRegisterType(_types)
               .withCotations(_cotations);

      } catch (Exception e) {
         logger.error("extractMother : {}",e.getMessage());
      } finally {
      }

      return _female;
   }

   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {
      
      // cas de l'objet topN
      if (_parameters.isTopN())
         return readVarietyTopN(_parameters.getYear(), _stats);
      
      // cas de l'objet topOfTheYear
      if (_parameters.isTopOfTheYear())
         return readVarietyTopOfTheYear(_parameters.getYear(), _stats);
      
      return readVariety(_stats);
   
   }
   
   /**
    * @param _stats              Liste des données de production à analyser
    * @return                    Propriété <code>variety</code> de l'objet <code>ParentFatherStatistics</code>
    */
   @SuppressWarnings("unchecked")
   private <T> T readVariety(List<T> _stats) {

      Map<TypeGender, ParentGender> _origins = new HashMap<TypeGender, ParentGender>();
      ParentFrequency _firstUse = null;
      
      try {
         // Caste la liste
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         ParentGender _statsFather = extractFather(_list);
         _origins.put(TypeGender.FATHER, _statsFather);
   
         ParentGender _statsMother = extractMother(_list);
         _origins.put(TypeGender.MOTHER, _statsMother);
   
         // lecture du nombre distinct de géniteurs
         long _totalFathers = _list
            .stream()
            .filter(StreamUtils.distinctByKey(BreederStatistics::getIdEtalon))
            .count();
         
         long _totalMothers = _list
               .stream()
               .filter(StreamUtils.distinctByKey(BreederStatistics::getIdLice))
               .count();
         
         // Lecture de la fréquence d'utilisation des géniteurs
         _firstUse = extractFrequency(_totalFathers, _totalMothers, _list);

      } catch (Exception e) {
         logger.error("readVariety",e.getMessage());
      } finally {
      }
         
      // Création de l'objet Variety
      return (T) new ParentVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withOrigins(_origins)
            .withFirstUse(_firstUse);      
      
   }
   
   /**
    * Retourne le classement des géniteurs ayant produit le plus de portée (ventilées sur les variétés de la race) dans l'année
    * 
    * @param _year         Année
    * @param _stats        Référentiel des meilleurs géniteurs sur les 5 dernières années
    * @return              Propriété <code>variety</code> de l'objet <code>ParentAffixVariety</code>
    */
   @SuppressWarnings("unchecked")
   private <T> T readVarietyTopN(int _year, List<T> _stats) {
      
      // Caste la liste
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      return (T) new ParentAffixVariety()
         .withId(this._idVariety)
         .withName(this._nameVariety)
         .withFathers(extractTopNVarietyOverYear(_year, _list));
      
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      
   // cas de l'objet topN
      if (_parameters.isTopN())
         return (T) new ParentAffixVariety()
               .withId(_variety.getId())
               .withName(_variety.getName())
               .withFathers(fullEmptyTopNVariety(_variety.getId()));
      
      // cas de l'objet topOfTheYear
      if (_parameters.isTopOfTheYear())
         return (T) new ParentAffixVariety()
               .withId(_variety.getId())
               .withName(_variety.getName())
               .withFathers(fullEmptyTopOfTheYearVariety(_variety.getId()));

      return (T) new ParentVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withOrigins(emptyParentOrigin())
            .withFirstUse(emptyParentFrequency());
   }

   /**
    * Retourne la fréquence d'utilisation d'un étalon dans les dossiers de portée
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>frequencies</code> de l'objet <code>ParentBreedStatistics</code>
    */
   private ParentFrequency extractFrequency(long _totalFathers, long _totalMothers, List<BreederStatistics> _list) {

      Map<TypeGender, ParentFrequencyDetail> _details = new HashMap<TypeGender, ParentFrequencyDetail>();
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);
      double _percent = 0;
      long _qtity = 0;
      
      try {
         
         ParentFrequencyDetail _statsFather = extractFrequencyFather(_totalFathers, _list);
         _details.put(TypeGender.FATHER, _statsFather);
   
         ParentFrequencyDetail _statsMother = extractFrequencyMother(_totalMothers, _list);
         _details.put(TypeGender.MOTHER, _statsMother);
   
         // Nb de geniteurs utilisés pour la première fois dans une saillie
         _qtity =_statsFather.getQtity() + _statsMother.getQtity();
         
         long _totalParents = _totalFathers + _totalMothers;
         if (_totalParents != 0) 
            _percent = Precision.round((double) _qtity / _totalParents, 4);

      } catch (Exception e) {
         logger.error("extractFrequency",e.getMessage());
      } finally {
      }
      
      return new ParentFrequency()
         .withQtity((int) _qtity)
         .withPercentage(format.format(_percent))
         .withDetails(_details);
         
   }

   private ParentFrequencyDetail extractFrequencyFather(long _totalFathers, List<BreederStatistics> _list) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);
      double _percent = 0;
      long _qtity = 0;

      try {
         // Nb d'étalons utilisés pour la première fois dans une saillie
         _qtity = _list.stream()
               .filter(x -> "O".equals(x.getPremiereSaillieEtalon()))
               .collect(Collectors.counting());
         
         if (_totalFathers != 0) 
            _percent = Precision.round((double) _qtity / _totalFathers, 4);

      } catch (Exception e) {
         logger.error("extractFrequencyFather",e.getMessage());
      } finally {
      }
      
      return new ParentFrequencyDetail()
            .withQtity((int) _qtity)
            .withPercentage(format.format(_percent));
   
   }

   private ParentFrequencyDetail extractFrequencyMother(long _totalMothers, List<BreederStatistics> _list) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);
      double _percent = 0;
      long _qtity = 0;
      
      try {
         // Nb de lices utilisées pour la première fois dans une saillie
         _qtity = _list.stream()
               .filter(x -> "O".equals(x.getPremiereSaillieLice()))
               .collect(Collectors.counting());
         
         if (_totalMothers != 0) 
            _percent = Precision.round((double) _qtity / _totalMothers, 4);
      
      } catch (Exception e) {
         logger.error("extractFrequencyMother",e.getMessage());
      } finally {
      }
      
      return new ParentFrequencyDetail()
            .withQtity((int) _qtity)
            .withPercentage(format.format(_percent));
   
   }

   /**
    * Retourne les données statistiques liées à la cotation des dossiers de portée
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>cotations</code> de l'objet <code>ParentBreedStatistics</code>
    */
   private List<ParentCotation> extractCotation(String sexe, List<BreederStatistics> _list) {

      List<ParentCotation> _cotationList = new ArrayList<ParentCotation>();
      int[] _cotReferences = new int[] { 1, 2, 3, 4, 5, 6 };
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);
      Map<Integer, Integer> _cotationsGeniteur = null;

      try {
         
         if ("M".equals(sexe)) {
            _cotationsGeniteur = _list.stream()
                  .collect(Collectors.groupingBy(BreederStatistics::getCotationEtalon
                           , Collectors.collectingAndThen(
                                 Collectors.mapping(BreederStatistics::getIdEtalon, Collectors.toSet()), Set::size)));
         } else {
            _cotationsGeniteur = _list.stream()
                  .collect(Collectors.groupingBy(BreederStatistics::getCotationLice, Collectors
                        .collectingAndThen(
                              Collectors.mapping(BreederStatistics::getIdLice, Collectors.toSet()), Set::size)));
         }
         
   
         double _total = _cotationsGeniteur.values().stream().mapToInt(Number::intValue).sum();
         double _percent = 0;
         double _arrondi = 0;
         
         // Suppression de la cotation traitée
         for (Map.Entry<Integer, Integer> _cot : _cotationsGeniteur.entrySet()) {
   
            _percent = Precision.round((double) _cot.getValue() / _total, 4);
            _arrondi += _percent;
            _cotReferences = ArrayUtils.removeElement(_cotReferences, _cot.getKey());
            
            ParentCotation c = new ParentCotation()
                  .withGrade(_cot.getKey())
                  .withQtity((int) (long) _cot.getValue())
                  .withPercentage(format.format(_percent));
            _cotationList.add(c);
         }
   
         // Gestion des arrondis
         if (_cotationList.size() > 1) {
            if (_arrondi != (double)1) {
               Stream<ParentCotation> stream = _cotationList.stream();
               stream.reduce((first, second) -> second)
                  .orElse(null)
                  .setPercentage(format.format(Precision.round(_percent,4)+Precision.round((1-_arrondi),4)));
            }
         }

         for (int i : _cotReferences) {
            ParentCotation c = new ParentCotation()
                  .withGrade(i)
                  .withQtity(0)
                  .withPercentage(format.format(0));
            _cotationList.add(c);
         }
   
         _cotationList.sort(Comparator.comparing(ParentCotation::getGrade));
      
      } catch (Exception e) {
         logger.error("extractCotation",e.getMessage());
      } finally {
      }
      
      return _cotationList;
   }

   /**
    * Retourne le classement des étalons s/ la race ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>fathers</code> de l'objet <code>ParentFatherStatistics</code>
    */
   private List<ParentFather> extractTopNBreedOverYear(int _year, List<BreederStatistics> _list) {

      List<ParentFather> _topNFathers = new ArrayList<ParentFather>();
      int position = 0;
      int qtityExaequo = 0;
      int currentPosition = 0;
      double _percent = 0;
      
      try {

         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);
         
         int _idBreed = _list.stream().findFirst().get().getIdRace();
         long _qtity = breederRepository.countByIdRaceAndAnnee(_idBreed, _year);
         
         // On ne sélectionne que les étalons contenus dans allTopNBreed
         // On groupe les étalons par qtites pour l'année en cours
         Map<Integer, Long> _fathers = _list
               .stream()
               .filter(StreamUtils.onlyTopNFather(this.allTopNBreed))
               .filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getIdEtalon, Collectors.counting()));

         // On sélectionne uniquement les étalons du top20 pour l'année en cours
         Set<ParentFather> _top20Fathers = this.allTopNBreed
               .stream()
               .filter(x -> _year == x.getYear())
               .collect(Collectors.toSet())
         ;
         
         // On trie la liste des étalons du topN pour l'année en cours
         Map<Integer, Long> result = _fathers.entrySet().stream()
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (oldValue, newValue) -> oldValue, LinkedHashMap::new));

         // On précise les positions
         for (Entry<Integer, Long> _father : result.entrySet()) {

            _percent = Precision.round((double) _father.getValue() / (double) _qtity, 4);
            
            // Si l'étalon n'est pas présent dans le top20, appartient-il au topN ?
            // si oui, il est sorti du classement pour l'année en cours; si non il n'est pas pris en compte
            // on conserve l'information qtités
            if (!isTop20(_father.getKey(), _top20Fathers)) {
               if (isTopN(_father.getKey())) {
                  _topNFathers.add(
                        new ParentFather()
                        .withId(_father.getKey())
                        .withName(getNameFatherFromBreed(_father.getKey()))
                        .withQtity((int) (long) _father.getValue())
                        .withPercentage(format.format(_percent))
                        .withPosition(0)
                     );
               }
               continue;
            }            

            if (qtityExaequo == (int) (long)_father.getValue() ) {
               position++;
            } else
               currentPosition = ++position;
            
            qtityExaequo = (int) (long)_father.getValue();
            _topNFathers.add(
                  new ParentFather()
                     .withId(_father.getKey())
                     .withName(getNameFatherFromBreed(_father.getKey()))
                     .withQtity(qtityExaequo)
                     .withPercentage(format.format(_percent))
                     .withPosition(currentPosition)
             );
            
         }

         // On complète la liste des étalons du topN manquants (attention, topN contient des doublons (prend en charge les années)
         Set<Integer> _idTopN = this.allTopNBreed.stream().map(p -> p.getId()).distinct().collect(Collectors.toSet());
         for (Integer _id : _idTopN) {
            if (!isPresent(_id, _topNFathers))
               _topNFathers.add(
                     new ParentFather()
                     .withId(_id)
                     .withName(getNameFatherFromBreed(_id))
                     .withQtity(0)
                     .withPercentage(format.format(0))
                     .withPosition(0)
                  );
         }

      } catch (Exception e) {
         logger.error("extractTopNBreedOverYear : {}",e.getMessage());
      } finally {
         // On trie les résultats par quantites décroissante
         _topNFathers.sort(Collections.reverseOrder(Comparator.comparing(ParentFather::getQtity)));
      }

      return _topNFathers;

   }
   
   private boolean isTop20(Integer _id, Set<ParentFather> _top20) {
      
      // L'étalon est-il présent dans le top20
      ParentFather x = _top20.stream()
            .filter(p -> p.getId() == _id)
            .findAny()
            .orElse(null);
      
      if (x == null)
         return false;
      else 
         return true;
   }

   private boolean isTopN(Integer _id) {
      
      List<ParentFather> _fathers = Collections.singletonList(new ParentFather().withId(_id));
      ParentFather y = this.allTopNBreed.stream()
            .filter(StreamUtils.isTopNFather(_fathers))
            .findAny()
            .orElse(null);
      if (y == null)
         return false;
      else
         return true;

   }
   
   private boolean isTopNVariety(Integer _id, List<ParentFather> _listFatherVarietyTopN) {
      
      List<ParentFather> _fathers = Collections.singletonList(new ParentFather().withId(_id));
      ParentFather y = _listFatherVarietyTopN.stream()
            .filter(StreamUtils.isTopNFather(_fathers))
            .findAny()
            .orElse(null);
      if (y == null)
         return false;
      else
         return true;

   }

   private boolean isPresent(Integer _id, List<ParentFather> _top) {
      
      ParentFather x = _top.stream()
            .filter(p -> p.getId() == _id)
            .findAny()
            .orElse(null);
      
      if (x == null)
         return false;
      else 
         return true;
   }

   /**
    * Retourne le classement des étalons s/ la variete ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>fathers</code> de l'objet <code>BreederAffixStatistics</code>
    */
   private List<ParentFather> extractTopNVarietyOverYear(int _year, List<BreederStatistics> _list) {
      
      List<ParentFather> _topNFathers = new ArrayList<ParentFather>();
      int position = 0;
      int qtityExaequo = 0;
      int currentPosition = 0;
      
      try {

         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);

         double _percent = 0;

         int _idBreed = _list.stream().findFirst().get().getIdRace();
         long _qtity = breederRepository.countByIdRaceAndAnnee(_idBreed, _year);
         
         // On extrait les étalons du topN varietes
         List<ParentFather> _listFatherVarietyTopN = this.allTopNVariety.entrySet()
               .stream()
               .filter(r -> r.getKey().getId() == this._idVariety)
               .flatMap(e -> e.getValue().stream())
               .collect(Collectors.toList())
         ;

         // On ne sélectionne que les affixes contenus dans allTopNVariety
         // On groupe les affixes par qtites pour la variété et l'année en cours
         Map<Integer, Long> _fathers = _list
               .stream()
               .filter(StreamUtils.onlyTopNFather(_listFatherVarietyTopN))
               .filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getIdEtalon, Collectors.counting()));

         // On sélectionne uniquement les étalons du top20 pour la variété et l'année en cours
         Set<ParentFather> _top20Fathers = _listFatherVarietyTopN
               .stream()
               .filter(x -> _year == x.getYear())
               .collect(Collectors.toSet())
         ;
         
         // On trie la liste des étalons du topN pour la variété et l'année en cours
         Map<Integer, Long> result = _fathers.entrySet().stream()
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (oldValue, newValue) -> oldValue, LinkedHashMap::new));

         
         // On précise les positions
         for (Entry<Integer, Long> _father : result.entrySet()) {

            _percent = Precision.round((double) _father.getValue() / (double) _qtity, 4);
            
            // Si l'étalon n'est pas présent dans le top20, appartient-il au topN ?
            // si oui, il est sorti du classement pour l'année en cours; si non il n'est pas pris en compte
            // on conserve l'information qtités
            if (!isTop20(_father.getKey(), _top20Fathers)) {
               if (isTopNVariety(_father.getKey(),_listFatherVarietyTopN)) {
                  _topNFathers.add(
                        new ParentFather()
                        .withId(_father.getKey())
                        .withName(getNameFatherFromBreed(_father.getKey()))
                        .withQtity((int) (long) _father.getValue())
                        .withPercentage(format.format(_percent))
                        .withPosition(0)
                     );
               }
               continue;
            }            

            if (qtityExaequo == (int) (long)_father.getValue() ) {
               position++;
            } else
               currentPosition = ++position;
            
            qtityExaequo = (int) (long)_father.getValue();
            _topNFathers.add(
                  new ParentFather()
                     .withId(_father.getKey())
                     .withName(getNameFatherFromBreed(_father.getKey()))
                     .withQtity(qtityExaequo)
                     .withPercentage(format.format(_percent))
                     .withPosition(currentPosition)
             );
            
         }

         // On complète la liste des étalons du topN manquants (attention, topN contient des doublons (prend en charge les années)
         Set<Integer> _idTopN = _listFatherVarietyTopN
               .stream()
               .map(p -> p.getId())
               .distinct()
               .collect(Collectors.toSet());
         
         for (Integer _id : _idTopN) {
            if (!isPresent(_id, _topNFathers))
               _topNFathers.add(
                     new ParentFather()
                     .withId(_id)
                     .withName(getNameFatherFromBreed(_id))
                     .withQtity(0)
                     .withPercentage(format.format(0))
                     .withPosition(0)
                  );
         }

      } catch (Exception e) {
         logger.error("extractTopNVarietyOverYear : {}",e.getMessage());
      } finally {
         // On trie les résultats par quantites décroissante
         _topNFathers.sort(Collections.reverseOrder(Comparator.comparing(ParentFather::getQtity)));
         
      }

      return _topNFathers;
      
   }
   
   /**
    * Lecture du nom de l'étalon
    * 
    * @param _id  Identifiant de l'etalon
    * @return
    */
   private String getNameFatherFromBreed(Integer _id) {
      String name = "";
      try {
         name = this.allTopOfTheYearBreed.stream().filter(p -> p.getId() == _id).findAny().orElse(null).getName();
      } catch (Exception e) {
         name = getNameFatherFromVariety(_id);
      } finally {
      }
      return name;
      
   } 

   private String getNameFatherFromVariety(Integer _id) {
      String name = "";
      try {
         name = this.allTopOfTheYearVariety.entrySet().stream().flatMap(e -> e.getValue().stream()).filter(p -> p.getId() == _id).findAny().orElse(null).getName();
      } catch (Exception e) {
         logger.error("getNameFather {} not found", _id);
      } finally {
      }
      return name;
   } 

   /**
    * Construction de la liste des étalons qui ont le plus produit sur les 5 dernières années
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Liste des étalons avec le nombre de portées
    */
   private void extractTopFathers(List<BreederStatistics> _list) {

      List<ParentFather> _sortedFathersTopNBreed = new ArrayList<ParentFather>();
      HashMap<TupleVariety,List<ParentFather>> _sortedFathersTopNVariety = new HashMap<TupleVariety,List<ParentFather>>();
      Set<ParentFather> _tmpFathersTopNVariety = new HashSet<ParentFather>();

      List<ParentFather> _sortedFathersTopOfTheYearBreed = new ArrayList<ParentFather>();
      HashMap<TupleVariety,List<ParentFather>> _sortedFathersTopOfTheYearVariety = new HashMap<TupleVariety,List<ParentFather>>();
      Set<ParentFather> _tmpFathersTopOfTheYearVariety = new HashSet<ParentFather>();

      try {
         // Sélection de l'année
         Map<Integer, List<BreederStatistics>> _breedGroupByYear = getYearStatistics(_list);
         for (Map.Entry<Integer, List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {
   
            // 1. On groupe les étalons par qtites
            Map<BreederStatistics, Long> _bestOfFathersBreedOverYear = _breedOverYear.getValue()
                  .stream()
                  .collect(Collectors.groupingBy(prd -> new BreederStatistics(prd.getIdEtalon(), prd.getNomEtalon()), Collectors.counting()))
            ;
   
            // 2.1 Classement général : on enregistre pour chaque année, les étalons ayant produit
            _sortedFathersTopOfTheYearBreed.addAll(
                  _bestOfFathersBreedOverYear.entrySet().stream()
                  .filter(x -> x.getValue() > 0 )
                  .map(a -> new ParentFather(a.getKey().getIdEtalon(), a.getKey().getNomEtalon(), _breedOverYear.getKey()))
                  .collect(Collectors.toList())
                  );

            // 2.2. On ne conserve que les 20 meilleurs que l'on ajouté à notre liste existante (l'objet Set nous prémunit des doublons)
            _sortedFathersTopNBreed.addAll(
                  _bestOfFathersBreedOverYear.entrySet().stream()
                     .filter(x -> x.getValue() > 0 )
                     .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                     .limit(this.limitTopN)
                     .map(a -> new ParentFather(a.getKey().getIdEtalon(), a.getKey().getNomEtalon(), _breedOverYear.getKey()))
                     .collect(Collectors.toList())
                     );
            
            // 3. On ajoute le classement général et le topN pour chacune des variétés
            // le groupement s/ fait s/ la variété de l'étalon
            Map<TupleVariety, List<BreederStatistics>> _allVariety = getVarietyFatherStatistics(_breedOverYear.getValue());
            for (Map.Entry<TupleVariety, List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {

               _tmpFathersTopNVariety.clear();
               _tmpFathersTopOfTheYearVariety.clear();
               
               Map<BreederStatistics, Long> _bestOfFathersVarietyOverYear = _currentVariety.getValue()
                     .stream()
                     .collect(Collectors.groupingBy(prd -> new BreederStatistics(prd.getIdEtalon(), prd.getNomEtalon()), Collectors.counting()));
               ;

               _tmpFathersTopOfTheYearVariety.addAll(
                     _bestOfFathersVarietyOverYear.entrySet().stream()
                       .filter(x -> x.getValue() > 0 )
                       .map(a -> new ParentFather(a.getKey().getIdEtalon(), a.getKey().getNomEtalon(), _breedOverYear.getKey()))
                       .collect(Collectors.toSet())
                       );
                 
               for (ParentFather p : _tmpFathersTopOfTheYearVariety) {
                 _sortedFathersTopOfTheYearVariety.computeIfAbsent(
                       _currentVariety.getKey()
                       , k -> new ArrayList<>()).add( 
                             new ParentFather(p.getId(), p.getName(), p.getYear())
                 );
               }
               
               _tmpFathersTopNVariety.addAll(
                   _bestOfFathersVarietyOverYear.entrySet().stream()
                     .filter(x -> x.getValue() > 0 )
                     .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                     .limit(this.limitTopN)
                     .map(a -> new ParentFather(a.getKey().getIdEtalon(), a.getKey().getNomEtalon(), _breedOverYear.getKey()))
                     .collect(Collectors.toSet())
                     );
               
               for (ParentFather p : _tmpFathersTopNVariety) {
                  _sortedFathersTopNVariety.computeIfAbsent(
                        _currentVariety.getKey()
                        , k -> new ArrayList<>()).add( 
                              new ParentFather(p.getId(), p.getName(), p.getYear())
                  );
               }
               
            }            
   
         }
   
         // On conserve le topN étalon (null-safe way)
         this.allTopNBreed = Optional.ofNullable(_sortedFathersTopNBreed)
               .map(List::stream)
               .orElseGet(Stream::empty)
               .collect(Collectors.toList());
   
         this.allTopNVariety = Optional.ofNullable(_sortedFathersTopNVariety)
               .map(x -> x.entrySet().stream())
               .orElseGet(Stream::empty)
               .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(oldValue, newValue) -> oldValue, LinkedHashMap::new));
         
         // Classement général
         this.allTopOfTheYearBreed = Optional.ofNullable(_sortedFathersTopOfTheYearBreed)
               .map(List::stream)
               .orElseGet(Stream::empty)
               .collect(Collectors.toList());
           
         this.allTopOfTheYearVariety = Optional.ofNullable(_sortedFathersTopOfTheYearVariety)
               .map(x -> x.entrySet().stream())
               .orElseGet(Stream::empty)
               .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(oldValue, newValue) -> oldValue, LinkedHashMap::new));

      } catch (Exception e) {
         logger.error("extractTopFathers",e.getMessage());
      } finally {
      }
      
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

      Map<TypeGender, ParentGender> _origins = new HashMap<TypeGender, ParentGender>();
      List<ParentVariety> _variety = null;
      ParentFrequency _firstUse = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         ParentGender _statsFather = extractFather(_list);
         _origins.put(TypeGender.FATHER, _statsFather);
   
         ParentGender _statsMother = extractMother(_list);
         _origins.put(TypeGender.MOTHER, _statsMother);
   
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list, new ParametersVariety(_year,false, false));
   
         // lecture du nombre distinct de géniteurs
         long _totalFathers = _list
            .stream()
            .filter(StreamUtils.distinctByKey(BreederStatistics::getIdEtalon))
            .count();
   
         long _totalMothers = _list
               .stream()
               .filter(StreamUtils.distinctByKey(BreederStatistics::getIdLice))
               .count();
   
         // Lecture de la fréquence d'utilisation des géniteurs
         _firstUse = extractFrequency(_totalFathers, _totalMothers, _list);

      } catch (Exception e) {
         logger.error("readYear",e.getMessage());
      } finally {
      }

      return (T) new ParentBreedStatistics()
            .withYear(_year)
            .withOrigins(_origins)
            .withFirstUse(_firstUse)
            .withVariety(_variety);

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new ParentBreedStatistics()
            .withYear(_year)
            .withOrigins(emptyParentOrigin())
            .withFirstUse(emptyParentFrequency())
            .withVariety(populateVarieties(new ArrayList<BreederStatistics>(), new ParametersVariety(_year,false,false)));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readTopN(List<T> _stats, int _year) {

      List<ParentFather> _topsN  = null;
      List<ParentAffixVariety> _topNVariety = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Recherche TopN Etalon de l'année en cours s/ la race et sur les varietes
         _topsN = extractTopNBreedOverYear(_year, _list);
         
         // Lecture TopN Etalon par variétés s/ la race en cours (et pour l'année en cours)      
         _topNVariety = populateVarieties(_list, new ParametersVariety(_year,true,false));
      
      } catch (Exception e) {
         logger.error("readTopN",e.getMessage());
      } finally {
      }
      
      return (T) new ParentFatherStatistics()
            .withYear(_year)
            .withFathers(_topsN)
            .withVariety(_topNVariety);    
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyTopN(int _year) {
      return (T) new ParentFatherStatistics()
            .withYear(_year)
            .withFathers(fullEmptyTopNBreed())
            .withVariety(populateVarieties(this._emptyParentsStatistics, new ParametersVariety(_year,true,false)));
   }
   
   /**
    * Retourne la liste des topN géniteurs s/ la race pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */
   private List<ParentFather> fullEmptyTopNBreed () {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return this.allTopNBreed.stream()
            .map(p -> new ParentFather(p.getId(), p.getName()))
            .distinct()
            .map (s -> new ParentFather(s.getId(), s.getName(), 0, format.format(0), 0))
            .collect(Collectors.toList());
   }   
   
   /**
    * Retourne la liste des topN géniteurs s/ la variété pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */   
   private List<ParentFather> fullEmptyTopNVariety (int idVariety) {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return this.allTopNVariety.entrySet()
            .stream()
            .filter(r -> r.getKey().getId() == idVariety)
            .flatMap(e -> e.getValue().stream())
            .map(p -> new ParentFather(p.getId(), p.getName()))
            .distinct()
            .map(s -> new ParentFather(s.getId(), s.getName(), 0, format.format(0), 0))
            .collect(ArrayList::new, ArrayList::add,ArrayList::addAll);            
            
   } 
   
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readBreed(List<T> _stats) {

      List<ParentBreedStatistics> _breedStatistics = null;
      List<ParentFatherStatistics> _fathersStatistics = null;
      List<ParentFatherStatistics> _topOfTheYearStatistics = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // On parcourt les années (on ajoute un tri)
         _breedStatistics = populateYears(_list);
   
         // Recherche TopN Etalon s/ la race
         // Regle de sélection :
         // Pour chaque année, on sélectionne le top 20.
         // On en déduit les 100 meilleurs étalons (les doublons sont supprimés) qui vont
         // nous servir de base pour construire le classement s/ chaque année
         setYearSeries(this._idBreed);
         extractTopFathers(_list);
         _fathersStatistics = populateTopN(_list);

         // Classement par année des étalons (pas de règle de sélection)
         _topOfTheYearStatistics = populateTopOfTheYear(_list);
         
      } catch (Exception e) {
         logger.error("readBreed",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Race
      return (T) new ParentBreed()
            .withId(this._idBreed)
            .withName(_nameBreed)
            .withStatistics(_breedStatistics)
            .withTopN(_fathersStatistics)
            .withTopOfTheYear(_topOfTheYearStatistics);
      
   }

   /**
    * Initialise l'objet ParentFrequency si pas de données statistiques
    *
    * @param 
    * @return
    */
   private ParentFrequency emptyParentFrequency() {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      int _qtity = 0;
      double _percent = 0;

      Map<TypeGender, ParentFrequencyDetail> _details = new HashMap<TypeGender, ParentFrequencyDetail>();
      _details.put(TypeGender.FATHER, emptyParentFrequencyDetail());
      _details.put(TypeGender.MOTHER, emptyParentFrequencyDetail());
      
      return new ParentFrequency()
            .withQtity(_qtity)
            .withPercentage(format.format(_percent))
            .withDetails(_details);

   }
   
   /**
    * Initialise l'objet ParentFrequencyDetail si pas de données statistiques
    *
    * @param 
    * @return
    */
   private ParentFrequencyDetail emptyParentFrequencyDetail() {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      int _qtity = 0;
      double _percent = 0;

      return new ParentFrequencyDetail()
            .withQtity(_qtity)
            .withPercentage(format.format(_percent));
   }
   
   /**
    * Initialise l'objet Origin (ParentFrequency) si pas de données statistiques
    *
    * @param 
    * @return
    */
   private Map<TypeGender, ParentGender> emptyParentOrigin() {

      Map<TypeGender, ParentGender> _origins = new HashMap<TypeGender, ParentGender>();
      _origins.put(TypeGender.FATHER, emptyParentGender(TypeGender.FATHER));
      _origins.put(TypeGender.MOTHER, emptyParentGender(TypeGender.MOTHER));

      return _origins;
   }
   
   /**
    * Initialise l'objet ParentGender si pas de données statistiques
    *
    * @param 
    * @return
    */
   private ParentGender emptyParentGender(TypeGender gender) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      int _qtity = 0;
      int _qtityType = 0;
      double _percent = 0;
      List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
      
      if (gender.equals(TypeGender.MOTHER)) {
         for (TypeRegistrationMother s : TypeRegistrationMother.values()) {
            
            ParentRegisterType _type = new ParentRegisterTypeMother()
                  .withRegistration(s)
                  .withQtity(_qtityType)
                  .withPercentage(format.format(_percent));
            _types.add(_type);
         }
      }
      
      if (gender.equals(TypeGender.FATHER)) {
         for (TypeRegistrationFather s : TypeRegistrationFather.values()) {
            
            ParentRegisterType _type = new ParentRegisterTypeFather()
                  .withRegistration(s)
                  .withQtity(_qtityType)
                  .withPercentage(format.format(_percent));
            _types.add(_type);
         }
      }
      
      return new ParentGender()
            .withQtity(_qtity)
            .withRegisterType(_types)
            .withCotations(emptyParentCotation());
   }
   
   /**
    * Initialise une liste d'objets ParentCotation si pas de données statistiques
    *
    * @param 
    * @return
    */
   private List<ParentCotation> emptyParentCotation() {

      List<ParentCotation> _cotationList = new ArrayList<ParentCotation>();
      int[] _cotReferences = new int[] { 1, 2, 3, 4, 5, 6 };
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

      for (int i : _cotReferences) {
         ParentCotation c = new ParentCotation()
               .withGrade(i)
               .withQtity(0)
               .withPercentage(format.format(0));
         _cotationList.add(c);
      }
      return _cotationList;
   }
   
   /**
    * Retourne le classement des étalons s/ la variete ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>fathers</code> de l'objet <code>BreederAffixStatistics</code>
    */
   private List<ParentFather> extractTopOfTheYearVarietyOverYear(int _year, List<BreederStatistics> _list) {
      
      List<ParentFather> _topOfTheYearFathers = new ArrayList<ParentFather>();
      int position = 0;
      int qtityExaequo = 0;
      int currentPosition = 0;
      
      try {

         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);

         double _percent = 0;

         int _idBreed = _list.stream().findFirst().get().getIdRace();
         long _qtity = breederRepository.countByIdRaceAndAnnee(_idBreed, _year);

         // On extrait les étalons par varietes
         List<ParentFather> _listFatherVarietyTopOfTheYear = this.allTopOfTheYearVariety.entrySet()
               .stream()
               .filter(r -> r.getKey().getId() == this._idVariety)
               .flatMap(e -> e.getValue().stream())
               .collect(Collectors.toList())
         ;         
         
         // On groupe les affixes par qtites pour la variété et l'année en cours
         Map<Integer, Long> _fathers = _list
               .stream()
               .filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getIdEtalon, Collectors.counting()));

         // On trie la liste des étalons pour la variété et l'année en cours
         Map<Integer, Long> result = _fathers.entrySet()
               .stream()
               .filter(x -> x.getValue() > 0 )
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (oldValue, newValue) -> oldValue, LinkedHashMap::new));

         
         // On précise les positions
         for (Entry<Integer, Long> _father : result.entrySet()) {

            _percent = Precision.round((double) _father.getValue() / (double) _qtity, 4);
            
            if (qtityExaequo == (int) (long)_father.getValue() ) {
               position++;
            } else
               currentPosition = ++position;
            
            qtityExaequo = (int) (long)_father.getValue();
            _topOfTheYearFathers.add(
                  new ParentFather()
                     .withId(_father.getKey())
                     .withName(getNameFatherFromBreed(_father.getKey()))
                     .withQtity(qtityExaequo)
                     .withPercentage(format.format(_percent))
                     .withPosition(currentPosition)
             );

         }
         
         // On complète la liste des étalons manquants (attention, topOfTheYear contient des doublons (prend en charge les années)
         Set<Integer> _idTopOfTheYear = _listFatherVarietyTopOfTheYear
               .stream()
               .map(p -> p.getId())
               .distinct()
               .collect(Collectors.toSet());
         
         for (Integer _id : _idTopOfTheYear) {
            if (!isPresent(_id, _topOfTheYearFathers))
               _topOfTheYearFathers.add(
                     new ParentFather()
                     .withId(_id)
                     .withName(getNameFatherFromBreed(_id))
                     .withQtity(0)
                     .withPercentage(format.format(0))
                     .withPosition(0)
                  );
         }         

      } catch (Exception e) {
         logger.error("extractTopOfTheYearVarietyOverYear : {}",e.getMessage());
      } finally {
         // On trie les résultats par quantites décroissante
         _topOfTheYearFathers.sort(Collections.reverseOrder(Comparator.comparing(ParentFather::getQtity)));
         
      }

      return _topOfTheYearFathers;
      
   }
   
   /**
    * Retourne le classement des géniteurs ayant produit le plus de portée (ventilées sur les variétés de la race) dans l'année
    * 
    * @param _year         Année
    * @param _stats        Référentiel des meilleurs géniteurs sur les 5 dernières années
    * @return              Propriété <code>variety</code> de l'objet <code>ParentAffixVariety</code>
    */
   @SuppressWarnings("unchecked")
   private <T> T readVarietyTopOfTheYear(int _year, List<T> _stats) {
      
      // Caste la liste
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      return (T) new ParentAffixVariety()
         .withId(this._idVariety)
         .withName(this._nameVariety)
         .withFathers(extractTopOfTheYearVarietyOverYear(_year, _list));
      
   }
   
   /**
    * Retourne le classement des étalons s/ la race ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>fathers</code> de l'objet <code>ParentFatherStatistics</code>
    */
   private List<ParentFather> extractTopOfTheYearBreedOverYear(int _year, List<BreederStatistics> _list) {

      List<ParentFather> _topOfTheYearFathers = new ArrayList<ParentFather>();
      int position = 0;
      int qtityExaequo = 0;
      int currentPosition = 0;
      double _percent = 0;
      
      try {

         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);
         
         int _idBreed = _list.stream().findFirst().get().getIdRace();
         long _qtity = breederRepository.countByIdRaceAndAnnee(_idBreed, _year);
         
         // On groupe les étalons par qtites pour l'année en cours
         Map<Integer, Long> _fathers = _list
               .stream()
               .filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getIdEtalon, Collectors.counting()));

         // On trie la liste des étalons pour l'année en cours
         Map<Integer, Long> result = _fathers.entrySet()
               .stream()
               .filter(x -> x.getValue() > 0 )
               .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
               .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                       (oldValue, newValue) -> oldValue, LinkedHashMap::new));

         // On précise les positions
         for (Entry<Integer, Long> _father : result.entrySet()) {

            _percent = Precision.round((double) _father.getValue() / (double) _qtity, 4);
            
            if (qtityExaequo == (int) (long)_father.getValue() ) {
               position++;
            } else
               currentPosition = ++position;
            
            qtityExaequo = (int) (long)_father.getValue();
            _topOfTheYearFathers.add(
                  new ParentFather()
                     .withId(_father.getKey())
                     .withName(getNameFatherFromBreed(_father.getKey()))
                     .withQtity(qtityExaequo)
                     .withPercentage(format.format(_percent))
                     .withPosition(currentPosition)
             );
            
         }
         
         // On complète la liste des étalons manquants (attention, topOfTheYear contient des doublons (prend en charge les années)
         Set<Integer> _idTopOfTheYear = this.allTopOfTheYearBreed.stream().map(p -> p.getId()).distinct().collect(Collectors.toSet());
         for (Integer _id : _idTopOfTheYear) {
            if (!isPresent(_id, _topOfTheYearFathers))
               _topOfTheYearFathers.add(
                     new ParentFather()
                     .withId(_id)
                     .withName(getNameFatherFromBreed(_id))
                     .withQtity(0)
                     .withPercentage(format.format(0))
                     .withPosition(0)
                  );
         }         

      } catch (Exception e) {
         logger.error("extractTopOfTheYearBreedOverYear : {}",e.getMessage());
      } finally {
         // On trie les résultats par quantites décroissante
         _topOfTheYearFathers.sort(Collections.reverseOrder(Comparator.comparing(ParentFather::getQtity)));
      }

      return _topOfTheYearFathers;

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readTopOfTheYear(List<T> _stats, int _year) {
      List<ParentFather> _topOfTheYear  = null;
      List<ParentAffixVariety> _topOfTheYearVariety = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Recherche Classement Etalon de l'année en cours s/ la race et sur les varietes
         _topOfTheYear = extractTopOfTheYearBreedOverYear(_year, _list);
         
         // Lecture Classement Etalon par variétés s/ la race en cours (et pour l'année en cours)      
         _topOfTheYearVariety = populateVarieties(_list, new ParametersVariety(_year,false,true));
      
      } catch (Exception e) {
         logger.error("readTopOfTheYear",e.getMessage());
      } finally {
      }
      
      return (T) new ParentFatherStatistics()
            .withYear(_year)
            .withFathers(_topOfTheYear)
            .withVariety(_topOfTheYearVariety);
      
   }

   /**
    * Retourne la liste des géniteurs s/ la variété pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */   
   private List<ParentFather> fullEmptyTopOfTheYearVariety (int idVariety) {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return this.allTopOfTheYearVariety.entrySet()
            .stream()
            .filter(r -> r.getKey().getId() == idVariety)
            .flatMap(e -> e.getValue().stream())
            .map(p -> new ParentFather(p.getId(), p.getName()))
            .distinct()
            .map(s -> new ParentFather(s.getId(), s.getName(), 0, format.format(0), 0))
            .collect(ArrayList::new, ArrayList::add,ArrayList::addAll);            
            
   } 
   
   /**
    * Retourne la liste des géniteurs s/ la race pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */
   private List<ParentFather> fullEmptyTopOfTheYearBreed () {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return this.allTopOfTheYearBreed.stream()
            .map(p -> new ParentFather(p.getId(), p.getName()))
            .distinct()
            .map (s -> new ParentFather(s.getId(), s.getName(), 0, format.format(0), 0))
            .collect(Collectors.toList());
   }   
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyTopOfTheYear(int _year) {
      return (T) new ParentFatherStatistics()
            .withYear(_year)
            .withFathers(fullEmptyTopOfTheYearBreed())
            .withVariety(populateVarieties(this._emptyParentsStatistics, new ParametersVariety(_year,false,true)));
   }
}
