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
import com.scc.lofselectclub.model.GenericStatistics;
import com.scc.lofselectclub.model.HealthStatistics;
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.repository.HealthRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleSupraMaladie;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.health.HealthBreed;
import com.scc.lofselectclub.template.health.HealthBreedStatistics;
import com.scc.lofselectclub.template.health.HealthFamily;
import com.scc.lofselectclub.template.health.HealthResponseObject;
import com.scc.lofselectclub.template.health.HealthResult;
import com.scc.lofselectclub.template.health.HealthTest;
import com.scc.lofselectclub.template.health.HealthType;
import com.scc.lofselectclub.template.health.HealthVariety;
import com.scc.lofselectclub.template.health.HealthVarietyStatistics;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.utils.TypeHealth;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class HealthService extends AbstractGenericService<HealthResponseObject,HealthStatistics> {

   public HealthService() {
      super();
      this.setGenericTemplate(new HealthResponseObject());
      this.setType(HealthStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

   @Autowired
   private HealthRepository healthRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;

   /**
    * Retourne les données statistiques liées à la santé pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>HealthResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackHealthList"
         , threadPoolKey = "getStatisticsHealth"
         , threadPoolProperties = {
            @HystrixProperty(name = "coreSize", value = "30"),
            @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
//                  @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "7000"),
                  @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
                  @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
                  @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
                  @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
                  @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }
         , ignoreExceptions = { EntityNotFoundException.class })
   public HealthResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the HealthService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

      try {
         
         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // On parcourt les races associées au club pour lesquelles des données ont été calculées
         List<HealthBreed> _breeds = populateBreeds(idClub);
         
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
    * @return        Objet <code>HealthResponseObject</code>
    */
   private HealthResponseObject buildFallbackHealthList(int idClub) {

      List<HealthBreed> list = new ArrayList<HealthBreed>();
      list.add(new HealthBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }

   /**
    * Retourne par type d'examen, les données statistiques par maladie
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>healthType</code> de l'objet <code>HealthFamily</code>
    * @see TypeHealth
    */
   private List<HealthType> extractHealthTestType(List<HealthStatistics> _list) {

      List<HealthType> _resultByType = new ArrayList<HealthType>();

      // On parcourt les résultats santé par type 
      Map<Integer, List<HealthStatistics>> _breedGroupByHealthType = _list.stream()
            .collect(StreamUtils.sortedGroupingBy(HealthStatistics::getNatureSuivi));
      for (Map.Entry<Integer, List<HealthStatistics>> _breedByHealthType : _breedGroupByHealthType.entrySet()) {

         // Lecture des maladies et de leurs résultats
         List<HealthFamily> _families = extractHealthFamily(_breedByHealthType.getValue());

         HealthType _type = new HealthType()
               .withType(TypeHealth.fromId(_breedByHealthType.getKey()))
               . withHealthFamily(_families);
         _resultByType.add(_type);
      }

      return _resultByType;
   }

   /**
    * Retourne par famille, les données statistiques par maladie
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>healthFamily</code> de l'objet <code>HealthBreedStatistics</code>
    * @see TypeHealth
    */
   private List<HealthFamily> extractHealthFamily(List<HealthStatistics> _list) {

      List<HealthFamily> _resultByFamily = new ArrayList<HealthFamily>();

      // On parcourt les résultats santé par famille
      Map<TupleSupraMaladie, List<HealthStatistics>> _breedGroupByHealthFamily = _list.stream()
            .collect(Collectors.groupingBy(r -> new TupleSupraMaladie(r.getCodeSupraMaladie(), r.getLibelleSupraMaladie())));
      for (Map.Entry<TupleSupraMaladie, List<HealthStatistics>> _breedByHealthFamily : _breedGroupByHealthFamily.entrySet()) {

         double _total = _breedByHealthFamily.getValue()
               .stream()
               .map(e -> e.getNbResultat()).reduce(0, (x, y) -> x + y);
         
         // On parcourt les années (on ajoute un tri)
         List<HealthBreedStatistics> _breedStatistics = populateYears(_breedByHealthFamily.getValue());

         HealthFamily _type = new HealthFamily()
               .withCode(_breedByHealthFamily.getKey().getCode())
               .withName(_breedByHealthFamily.getKey().getName())
               .withQtity((int) _total)
               .withStatistics(_breedStatistics);
         _resultByFamily.add(_type);
      }

      return _resultByFamily;
   }

   
   /**
    * Retourne la liste des maladies pour un type de maladie
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>healthTest</code> de l'objet <code>HealthType</code>
    */
   private List<HealthTest> extractHealthTest(List<HealthStatistics> _list) {

      List<HealthTest> _resultByType = new ArrayList<HealthTest>();

      // On parcourt les maladies
      Map<TupleSupraMaladie, List<HealthStatistics>> _breedGroupByHealthResult = _list.stream()
            .collect(Collectors.groupingBy(r -> new TupleSupraMaladie(r.getCodeMaladie(), r.getLibelleMaladie())));
      for (Map.Entry<TupleSupraMaladie, List<HealthStatistics>> _breedByHealthTest : _breedGroupByHealthResult.entrySet()) {

         double _total = _breedByHealthTest.getValue()
               .stream()
               .map(e -> e.getNbResultat()).reduce(0, (x, y) -> x + y);

         // Lecture des résultats pour la maladie en cours
         List<HealthResult> _healthResults = extractHealthResult(_breedByHealthTest.getValue(), _total);

         String _code = _breedByHealthTest.getKey().getCode();
         String _name = _breedByHealthTest.getKey().getName();

         HealthTest _i = new HealthTest()
               .withCode(_code)
               .withName(_name)
               .withQtity((int) _total)
               .withHealthResults(_healthResults);

         _resultByType.add(_i);

      }

      return _resultByType;
   }

   /**
    * Retourne la liste des résultats pour une maladie
    * 
    * @param _list   Liste des données de production à analyser
    * @param _total  Nombre de résultats santé pour le test
    * @return        Propriété <code>healthResults</code> de l'objet <code>HealthTest</code>
    */
   private List<HealthResult> extractHealthResult(List<HealthStatistics> _list, double _total) {

      List<HealthResult> _resultByType = new ArrayList<HealthResult>();

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      double _percent = 0;
            
      // On parcourt les résultats
      Map<TupleSupraMaladie, List<HealthStatistics>> _breedGroupByHealthResult = _list.stream()
            .collect(Collectors.groupingBy(r -> new TupleSupraMaladie(r.getCodeResultat(), r.getLibelleResultat())));
      for (Map.Entry<TupleSupraMaladie, List<HealthStatistics>> _breedByResult : _breedGroupByHealthResult.entrySet()) {

         String _code = _breedByResult.getKey().getCode();
         String _name = _breedByResult.getKey().getName();
         
         int _sort = _breedByResult.getValue()
               .stream()
               .findFirst()
               .map(HealthStatistics::getTriResultat)
               .orElse(0)
         ;
         
         int _qtity = _breedByResult.getValue()
               .stream()
               .map(e -> e.getNbResultat()).reduce(0, (x, y) -> x + y);
         
         if (_total > 0)
            _percent = Precision.round((double) _qtity / _total, 2);

         List<HealthVariety> _variety = populateVarieties(_breedByResult.getValue(), new ParametersVariety(_qtity));

         HealthResult _t = new HealthResult()
               .withCode(_code)
               .withName(_name)
               .withQtity(_qtity)
               .withPercentage(format.format(_percent))
               .withVariety(_variety)
               .withSort(_sort);

         _resultByType.add(_t);

      }
      
      // tri des résultats
      _resultByType.sort(Comparator.comparing(HealthResult::getSort));

      return _resultByType;
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      double _percent = 0;

      // Caste la liste
      List<HealthStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      int _qtity = _list.stream()
            .map(e -> e.getNbResultat()).reduce(0, (x, y) -> x + y);
      
      if (_parameters.getTotal() > 0 )
         _percent = Precision.round((double) _qtity / _parameters.getTotal(), 2);

      // Création de l'objet VarietyStatistics
      HealthVarietyStatistics _varietyStatistics = new HealthVarietyStatistics()
            .withQtity(_qtity)
            .withPercentage(format.format(_percent));
      
      // Création de l'objet Variety
      return (T) new HealthVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withStatistics(_varietyStatistics);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      return (T) new HealthVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withStatistics(new HealthVarietyStatistics()
                  .withQtity(0)
                  .withPercentage(format.format(0))
                  );
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M getDataStatistics(int idClub) {
      return 
            (M) healthRepository.findByIdClub(idClub)
            .stream()
            .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readYear(List<T> _stats, int _year) {
     
      List<HealthStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Lecture des maladies et de leurs résultats
      List<HealthTest> _test = extractHealthTest(_list);

      return (T) new HealthBreedStatistics()
            .withYear(_year)
            .withHealthTest(_test);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new HealthBreedStatistics()
            .withYear(_year)
            .withHealthTest(extractHealthTest(new ArrayList<HealthStatistics>()));
   }

   @Override
   protected <T> T readTopN(List<T> _stats, int _year) {
      return null;
   }

   @Override
   protected <T> T emptyTopN(int _year) {
      return null;
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readBreed(List<T> _stats) {

      List<HealthStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      //List<HealthFamily> _families = extractHealthFamily(_list);
      List<HealthType> _types = extractHealthTestType(_list);
      
      // Création de l'objet Race
      return (T) new HealthBreed()
            .withId(this._idBreed)
            .withName(this._nameBreed)
            .withHealthType(_types);

   }   
}
