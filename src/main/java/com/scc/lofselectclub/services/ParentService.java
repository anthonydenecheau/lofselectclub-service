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
import com.scc.lofselectclub.repository.ConfirmationRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.parent.ParentVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.utils.TypeRegistration;
import com.scc.lofselectclub.template.parent.ParentFather;
import com.scc.lofselectclub.template.parent.ParentBreed;
import com.scc.lofselectclub.template.parent.ParentResponseObject;
import com.scc.lofselectclub.template.parent.ParentBreedStatistics;
import com.scc.lofselectclub.template.parent.ParentCotation;
import com.scc.lofselectclub.template.parent.ParentFatherStatistics;
import com.scc.lofselectclub.template.parent.ParentFrequency;
import com.scc.lofselectclub.template.parent.ParentGender;
import com.scc.lofselectclub.template.parent.ParentRegisterType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
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
   private ConfirmationRepository confirmationRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;

   private int limitTopN = 0;
   private Set<ParentFather> allTopN = new HashSet<ParentFather>();

   /**
    * Retourne les données statistiques liées aux géniteurs pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>ParentResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackParentList"
         , threadPoolKey = "getStatistics"
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
    * @see TypeRegistration
    */
   private List<ParentGender> extractFather(List<BreederStatistics> _list) {

      List<ParentGender> _stats = new ArrayList<ParentGender>();

      int _qtity = 0;
      int _qtityTypeFrancais = 0;
      int _qtityTypeImport = 0;
      int _qtityTypeEtranger = 0;
      int _qtityTypeAutre = 0;

      try {

         // Male : Groupement par type d'inscription (sans doublons)
         Map<Integer, Integer> _map = _list.stream()
               .collect(Collectors.groupingBy(BreederStatistics::getTypeEtalon
                           , Collectors.collectingAndThen(
                                Collectors.mapping(BreederStatistics::getIdEtalon, Collectors.toSet()), Set::size)));

         for (Integer key : _map.keySet()) {
            Integer value = _map.get(key);

            switch (TypeRegistration.fromId(key)) {
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
               _qtityTypeAutre += value;
               break;
            }

            // total tous types confondus
            _qtity += value;
         }

         List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

         for (TypeRegistration s : TypeRegistration.values()) {
            int _qtityType = 0;
            double _percent = 0;
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
               _qtityType = _qtityTypeAutre;
               break;
            }

            _percent = Precision.round((double) _qtityType / (double) _qtity, 2);
            
            ParentRegisterType _type = new ParentRegisterType()
                  .withRegistration(s)
                  .withQtity(_qtityType)
                  .withPercentage(format.format(_percent));
            _types.add(_type);
         }


         // Lecture du nb de géniteur par cotations
         List<ParentCotation> _cotations = extractCotation("M", _list);
         
         ParentGender _male = new ParentGender()
               .withQtity(_qtity)
               .withRegisterType(_types)
               .withCotations(_cotations);
         _stats.add(_male);

      } finally {

      }

      return _stats;
   }

   /**
    * Retourne la répartition des lices par type d'inscription
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>origins</code> de l'objet <code>ParentBreedStatistics</code>
    * @see TypeRegistration
    */
   private List<ParentGender> extractMother(List<BreederStatistics> _list) {

      List<ParentGender> _stats = new ArrayList<ParentGender>();

      int _qtity = 0;
      int _qtityTypeFrancais = 0;
      int _qtityTypeImport = 0;
      int _qtityTypeAutre = 0;

      try {

         // Femelle : Groupement par type d'inscription (sans doublons)
         Map<Integer, Integer> _map = _list.stream()
               .collect(Collectors.groupingBy(BreederStatistics::getTypeLice
                        , Collectors.collectingAndThen(
                              Collectors.mapping(BreederStatistics::getIdLice, Collectors.toSet()), Set::size)));

         for (Integer key : _map.keySet()) {
            Integer value = _map.get(key);

            switch (TypeRegistration.fromId(key)) {
            case FRANCAIS:
               _qtityTypeFrancais += value;
               break;
            case IMPORTES:
               _qtityTypeImport += value;
               break;
            default:
               _qtityTypeAutre += value;
               break;
            }

            // total tous types confondus
            _qtity += value;
         }

         List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

         for (TypeRegistration s : TypeRegistration.values()) {
            int _qtityType = 0;
            double _percent = 0;
            switch (s) {
            case FRANCAIS:
               _qtityType = _qtityTypeFrancais;
               break;
            case IMPORTES:
               _qtityType = _qtityTypeImport;
               break;
            default:
               _qtityType = _qtityTypeAutre;
               break;
            }

            _percent = Precision.round((double) _qtityType / (double) _qtity, 2);
            
            ParentRegisterType _type = new ParentRegisterType()
                  .withRegistration(s)
                  .withQtity(_qtityType)
                  .withPercentage(format.format(_percent));
            _types.add(_type);
         }

         // Lecture du nb de géniteur par cotations
         List<ParentCotation> _cotations = extractCotation("F", _list);
         
         ParentGender _female = new ParentGender()
               .withQtity(_qtity)
               .withRegisterType(_types)
               .withCotations(_cotations);
         _stats.add(_female);

      } finally {

      }

      return _stats;
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {
      
      // Caste la liste
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      List<Map<String, List<ParentGender>>> _origins = new ArrayList<Map<String, List<ParentGender>>>();
      Map<String, List<ParentGender>> _origin = new HashMap<String, List<ParentGender>>();

      List<ParentGender> _statsFather = extractFather(_list);
      _origin.put("father", _statsFather);

      List<ParentGender> _statsMother = extractMother(_list);
      _origin.put("mother", _statsMother);

      _origins.add(_origin);

      // lecture du nombre de confirmation
      int year = _list.stream().findFirst().get().getAnnee();
      long _totalConfirmaton = confirmationRepository.findByIdVarieteAndAnneeAndSexe(this._idVariety, year, "M")
         .stream()
         .collect(Collectors.counting());
      
      // Lecture de la fréquence d'utilisation du géniteur (uniquement étalon)
      List<ParentFrequency> _frequencies = extractFrequency(_totalConfirmaton, _list);
      
      // Création de l'objet Variety
      return (T) new ParentVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withOrigins(_origins)
            .withFrequencies(_frequencies);

   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      return (T) new ParentVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withOrigins(new ArrayList<Map<String, List<ParentGender>>>())
            .withFrequencies(new ArrayList<ParentFrequency>());
   }

   /**
    * Retourne la fréquence d'utilisation d'un étalon dans les dossiers de portée
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>frequencies</code> de l'objet <code>ParentBreedStatistics</code>
    */
   private List<ParentFrequency> extractFrequency(long _totalConfirmation, List<BreederStatistics> _list) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      double _percent = 0;
      
      List<ParentFrequency> _frequencyList = new ArrayList<ParentFrequency>();

      TreeMap<Integer, Integer> _series = new TreeMap<Integer, Integer>();

      // Nb d'étalons utilisés pour la première fois dans une saillie
      long _qtity = _list.stream()
            .filter(x -> "O".equals(x.getFirstSaillie()))
            .collect(Collectors.counting());
      
      if (_totalConfirmation != 0) 
         _percent = Precision.round((double) _qtity / _totalConfirmation, 2);
      
      _frequencyList.add (new ParentFrequency()
               .withTime(1)
               .withQtity((int) _qtity)
               .withPercentage(format.format(_percent))
      );

      return _frequencyList;
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
      Map<Integer, Integer> _cotationsGeniteur = null;
      
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

      // Suppression de la cotation traitée
      for (Map.Entry<Integer, Integer> _cot : _cotationsGeniteur.entrySet()) {

         _percent = Precision.round((double) _cot.getValue() / _total, 2);
         _cotReferences = ArrayUtils.removeElement(_cotReferences, _cot.getKey());
         
         ParentCotation c = new ParentCotation()
               .withGrade(_cot.getKey())
               .withQtity((int) (long) _cot.getValue())
               .withPercentage(format.format(_percent));
         _cotationList.add(c);
      }

      for (int i : _cotReferences) {
         ParentCotation c = new ParentCotation()
               .withGrade(i)
               .withQtity(0)
               .withPercentage(format.format(0));
         _cotationList.add(c);
      }

      _cotationList.sort(Comparator.comparing(ParentCotation::getGrade));

      return _cotationList;
   }

   // Voir Stackoverflow :
   // https://stackoverflow.com/questions/52343325/java-8-stream-how-to-get-top-n-count?noredirect=1#comment91673408_52343325
   // private List<ParentFather> extractTopNOverYear (int _limitTopN,
   // List<BreederStatistics> _list) {
   //
   // List<ParentFather> _topNFathers = new ArrayList<ParentFather>();
   //
   // Map<BreederStatistics, Long> counts = _list
   // .stream()
   // .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
   //
   // Collection<BreederStatistics> _topN = StreamUtils.topN(counts, _limitTopN,
   // BreederStatistics::getIdEtalon);
   //
   // Map<String, Long> _resume = _topN
   // .stream()
   // .collect(
   // Collectors.groupingBy(
   // BreederStatistics::getNomEtalon, Collectors.counting()
   // )
   // );
   //
   // for (Map.Entry<String, Long> entry : _resume.entrySet()) {
   // ParentFather _currentEtalon = new ParentFather()
   // .withName(entry.getKey())
   // .withQtity((int) (long)entry.getValue())
   // ;
   // _topNFathers.add(_currentEtalon);
   // }
   //
   //
   // return _topNFathers;
   // }

   /**
    * Retourne le classement des étalons ayant produit le plus de portées dans l'année
    * 
    * @param _year   Année
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>fathers</code> de l'objet <code>ParentFatherStatistics</code>
    */
   private List<ParentFather> extractTopNOverYear(int _year, List<BreederStatistics> _list) {

      List<ParentFather> _topNFathers = new ArrayList<ParentFather>();
      
      try {

         NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
         format.setMaximumFractionDigits(2);
         format.setMinimumFractionDigits(2);

         double _percent = 0;
         
         // 1. On groupe les étalons par qtites pour l'année en cours
         Map<Integer, Long> _fathers = _list.stream().filter(x -> (_year == x.getAnnee()))
               .collect(Collectors.groupingBy(BreederStatistics::getIdEtalon, Collectors.counting()));
         ;

         // 3. On complète par les étalons potentiellement manquants
         boolean g = false;
         for (ParentFather _fatherTopN : this.allTopN) {
            g = false;
            for (Map.Entry<Integer, Long> entry : _fathers.entrySet()) {
               if (_fatherTopN.getId() == entry.getKey()) {
                  g = true;
                  break;
               }
            }

            if (!g) {
               ParentFather _currentEtalon = new ParentFather()
                     .withId(_fatherTopN.getId())
                     .withName(_fatherTopN.getName())
                     .withQtity(0)
                     .withPercentage(format.format(0));
               _topNFathers.add(_currentEtalon);
            }
         }

         int _idBreed = _list.stream().findFirst().get().getIdRace();
         long _qtity = breederRepository
               .findByIdRaceAndAnnee(_idBreed, _year)
               .stream()
               .collect(Collectors.counting())
         ;
         
         // 2. On alimente notre Map
         for (Entry<Integer, Long> _father : _fathers.entrySet()) {
            
            _percent = Precision.round((double) _father.getValue() / (double) _qtity, 4);
            ParentFather _currentFather = new ParentFather()
                  .withId(_father.getKey())
                  .withName(getNameFather(_father.getKey()))
                  .withQtity((int) (long) _father.getValue())
                  .withPercentage(format.format(_percent));
            _topNFathers.add(_currentFather);
         }

         // 4. On trie les résultats par quantites décroissante
         _topNFathers.sort(Collections.reverseOrder(Comparator.comparing(ParentFather::getQtity)));
         
      } catch (Exception e) {
         System.out.println(e.getMessage());
      }

      return _topNFathers;

   }
   
   /**
    * Lecture du nom de l'étalon
    * 
    * @param _id  Identifiant de l'etalon
    * @return
    */
   private String getNameFather(Integer _id) {
      return this.allTopN.stream().filter(p -> p.getId() == _id).findAny().orElse(null).getName();
   } 

   /**
    * Construction de la liste des étalons qui ont le plus produit sur les 5 dernières années
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Liste des étalons avec le nombre de portées
    */
   private List<BreederStatistics> extractTopNFathers(List<BreederStatistics> _list) {

      List<BreederStatistics> _topNFathers = new ArrayList<BreederStatistics>();
      Set<ParentFather> _sortedFathers = new HashSet<ParentFather>();

      // Sélection de l'année
      Map<Integer, List<BreederStatistics>> _breedGroupByYear = getYearStatistics(_list);
      for (Map.Entry<Integer, List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

         // 1. On groupe les étalons par qtites
         Map<BreederStatistics, Long> _bestOfFathersOverYear = _breedOverYear.getValue().stream().collect(Collectors
               .groupingBy(prd -> new BreederStatistics(prd.getIdEtalon(), prd.getNomEtalon()), Collectors.counting()));
         ;

         // 2. On ne conserve que les 20 meilleurs que l'on ajouté à notre liste existante (l'objet Set nous prémunit des doublons)
         _sortedFathers.addAll(
               _bestOfFathersOverYear.entrySet().stream()
                  .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                  .limit(this.limitTopN)
                  .map(a -> new ParentFather(a.getKey().getIdEtalon(), a.getKey().getNomEtalon()))
                  .collect(Collectors.toSet())
                  );

      }

      // On conserve le topN étalon (null-safe way)
      this.allTopN = Optional.ofNullable(_sortedFathers)
            .map(Set::stream)
            .orElseGet(Stream::empty)
            .collect(Collectors.toSet());

      // 3. On (re)construit la liste qui sera utilisée pour la lecture des filtres par année et/ou par mois.
      // Cette liste ne contient que les données des étalons préselectionnés
      _topNFathers = StreamUtils.filterTopNFathers(_list, StreamUtils.isTopNFather(_sortedFathers));
      
      return _topNFathers;
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M getDataStatistics(int idClub) {
      return 
            (M) breederRepository.findByIdClub(idClub)
            .stream()
            .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readYear(List<T> _stats, int _year) {

      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      List<Map<String, List<ParentGender>>> _origins = new ArrayList<Map<String, List<ParentGender>>>();
      Map<String, List<ParentGender>> _origin = new HashMap<String, List<ParentGender>>();

      List<ParentGender> _statsFather = extractFather(_list);
      _origin.put("father", _statsFather);

      List<ParentGender> _statsMother = extractMother(_list);
      _origin.put("mother", _statsMother);

      _origins.add(_origin);

      // Lecture des variétés s/ la race en cours (et pour l'année en cours)
      List<ParentVariety> _variety = populateVarieties(_list, null);

      // lecture du nombre de confirmation
      long _totalConfirmaton = confirmationRepository.findByIdRaceAndAnneeAndSexe(this._idBreed, _year, "M")
         .stream()
         .collect(Collectors.counting());
      
      // Lecture de la fréquence d'utilisation du géniteur (uniquement étalon)
      List<ParentFrequency> _frequencies = extractFrequency(_totalConfirmaton, _list);

      return (T) new ParentBreedStatistics()
            .withYear(_year)
            .withOrigins(_origins)
            .withFrequencies(_frequencies)
            .withVariety(_variety);

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new ParentBreedStatistics()
            .withYear(_year)
            .withOrigins(new ArrayList<Map<String, List<ParentGender>>>())
            .withVariety(populateVarieties(new ArrayList<BreederStatistics>(), null));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readTopN(List<T> _stats, int _year) {

      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Recherche TopN Etalon de l'année en cours s/ la race et sur les varietes
      List<ParentFather> _topsN = extractTopNOverYear(_year, _list);
      return (T) new ParentFatherStatistics()
            .withYear(_year)
            .withFathers(_topsN);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyTopN(int _year) {
      return (T) new ParentFatherStatistics()
            .withYear(_year)
            .withFathers(fullEmptyTopN());
   }
   
   /**
    * Retourne la liste des topN géniteurs pour lesquels aucune production n'a été enregistrée
    * 
    * @return
    */
   private List<ParentFather> fullEmptyTopN () {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return this.allTopN.stream()
            .map (s -> new ParentFather(s.getId(), s.getName(), 0, format.format(0)))
            .collect(Collectors.toList());
   }   

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readBreed(List<T> _stats) {

      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // On parcourt les années (on ajoute un tri)
      List<ParentBreedStatistics> _breedStatistics = populateYears(_list);

      // Recherche TopN Etalon s/ la race
      // Regle de sélection :
      // Pour chaque année, on sélectionne le top 20.
      // On en déduit les 100 meilleurs étalons (les doublons sont supprimés) qui vont
      // nous servir de base pour construire le classement s/ chaque année
      setYearSeries(this._idBreed);
      List<BreederStatistics> _topsNFathers = extractTopNFathers(_list);
      List<ParentFatherStatistics> _fathersStatistics = populateTopN(_topsNFathers);
      
      // Création de l'objet Race
      return (T) new ParentBreed()
            .withId(this._idBreed)
            .withName(_nameBreed)
            .withStatistics(_breedStatistics)
            .withTopN(_fathersStatistics);
      
   }

}
