package com.scc.lofselectclub.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.model.PerformanceStatistics;
import com.scc.lofselectclub.repository.PerformanceRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TuplePerformance;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.performance.PerformanceBreed;
import com.scc.lofselectclub.template.performance.PerformanceBreedStatistics;
import com.scc.lofselectclub.template.performance.PerformanceResponseObject;
import com.scc.lofselectclub.template.performance.PerformanceResult;
import com.scc.lofselectclub.template.performance.PerformanceTypeDetail;
import com.scc.lofselectclub.template.performance.PerformanceVariety;
import com.scc.lofselectclub.utils.TypeGender;
import com.scc.lofselectclub.utils.TypePerformance;

@Service
@Transactional
public class PerformanceService extends AbstractGenericService<PerformanceResponseObject,PerformanceStatistics> {

   public PerformanceService() {
      super();
      this.setGenericTemplate(new PerformanceResponseObject());
      this.setType(PerformanceStatistics.class);
   }
   
   private static final Logger logger = LoggerFactory.getLogger(BreederService.class);

   @Autowired
   private PerformanceRepository performanceRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;
   
   /**
    * Retourne les données statistiques liées à l'élevage pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>BreederResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackPerformanceList"
         , threadPoolKey = "getStatisticsPerformance"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public PerformanceResponseObject getStatistics(int idClub) throws EntityNotFoundException {
      
      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the PerformanceService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // On parcourt la liste des races associées au club pour lesquelles des données ont été calculées
         List<PerformanceBreed> _breeds = populateBreeds(idClub);
               
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
    * @return        Objet <code>PerformanceResponseObject</code>
    */
   private PerformanceResponseObject buildFallbackPerformanceList(int idClub) {

      List<PerformanceBreed> list = new ArrayList<PerformanceBreed>();
      list.add(new PerformanceBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }
   

   @SuppressWarnings("unchecked")
   @Override
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M getDataStatistics(int idClub) {
      return 
            (M) performanceRepository.findByIdClub(idClub, orderByTri())
            .stream()
            .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      Map<TypePerformance, PerformanceTypeDetail> _performances = null;

      try {
         // Caste la liste
         List<PerformanceStatistics> _list = feed((List<? extends GenericStatistics>) _stats);

         // Lecture des performances regroupées par type
         _performances = extractTypePerformance(_list);

      } catch (Exception e) {
         logger.error("readVariety : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Variety
      return (T) new PerformanceVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withPerformances(_performances);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      return (T) new PerformanceVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withPerformances(null);

   }

   private  List<PerformanceResult> extractResultPerformance(List<PerformanceStatistics> _list) {

      List<PerformanceResult> _resultByType = new ArrayList<PerformanceResult>();
      
      try {
         // On parcourt les résultats par Niveau/Titre
         Map<TuplePerformance, List<PerformanceStatistics>> _breedGroupByPerformanceType = _list.stream()
               .collect(Collectors.groupingBy(r -> new TuplePerformance(r.getIdKey(), r.getCodeKey(), r.getNameKey())));
         for (Map.Entry<TuplePerformance, List<PerformanceStatistics>> _breedByPerformanceType : _breedGroupByPerformanceType.entrySet()) {
         
            Map<TypeGender, Integer> gender = new HashMap<TypeGender, Integer>();

            double _total = _breedByPerformanceType.getValue()
                  .stream()
                  .map(e -> e.getQtity()).reduce(0, (x, y) -> x + y);
            
            double _totalMale = _breedByPerformanceType.getValue()
                  .stream()
                  .filter(e -> e.getSexe().equals("M"))
                  .map(e -> e.getQtity()).reduce(0, (x, y) -> x + y);
            gender.put(TypeGender.FATHER, (int)_totalMale);
            
            double _totalFemale = _breedByPerformanceType.getValue()
                  .stream()
                  .filter(e -> e.getSexe().equals("F"))
                  .map(e -> e.getQtity()).reduce(0, (x, y) -> x + y);
            gender.put(TypeGender.MOTHER, (int)_totalFemale);
             
            PerformanceResult _type = new PerformanceResult()
                  .withCode(_breedByPerformanceType.getKey().getCode())
                  .withName(_breedByPerformanceType.getKey().getName())
                  .withQtity((int)_total)
                  .withGender(gender);
            _resultByType.add(_type);
            
         }
      } catch (Exception e) {
         logger.error("extractResultPerformance : {}",e.getMessage());
      } finally {
      }
      
      return _resultByType;
   }

   private PerformanceTypeDetail extractDetailPerformance(List<PerformanceStatistics> _list) {

      PerformanceTypeDetail _detail = null;
      PerformanceStatistics sumPerformance = null;
      Map<TypeGender, Integer> gender = new HashMap<TypeGender, Integer>();
      
      int _qtity = 0; 
      
      try {

         // Somme des resultats
         sumPerformance = _list.stream().reduce(new PerformanceStatistics(0),
               (x, y) -> {
                  return new PerformanceStatistics(
                        x.getQtity() + y.getQtity());
               });
         
         _qtity = sumPerformance.getQtity();
         
         double _totalMale = _list
               .stream()
               .filter(e -> e.getSexe().equals("M"))
               .map(e -> e.getQtity()).reduce(0, (x, y) -> x + y);
         gender.put(TypeGender.FATHER, (int)_totalMale);
         
         double _totalFemale = _list
               .stream()
               .filter(e -> e.getSexe().equals("F"))
               .map(e -> e.getQtity()).reduce(0, (x, y) -> x + y);
         gender.put(TypeGender.MOTHER, (int)_totalFemale);
         
         List<PerformanceResult> results = extractResultPerformance(_list);
         
         _detail = new PerformanceTypeDetail()
               .withQtity(_qtity)
               .withGender(gender)
               .withResults(results)
         ;
         
      } catch (Exception e) {
         logger.error("extractDetailPerformance : {}",e.getMessage());
      } finally {
      }
      
      return _detail;
   }
   
   /**
    * Retourne la liste des performances pour un type donné
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>PerformanceTypeDetail</code> de l'objet <code>PerformanceBreedStatistics</code>
    */
   private Map<TypePerformance, PerformanceTypeDetail> extractTypePerformance(List<PerformanceStatistics> _list) {
      
      Map<TypePerformance, PerformanceTypeDetail> _performances = new HashMap<TypePerformance, PerformanceTypeDetail>();
      List<String> enumType = Stream.of(TypePerformance.values())
            .map(Enum::name)
            .collect(Collectors.toList());
      
      try { 
         
         // On parcourt les performances
         Map<Integer, List<PerformanceStatistics>> _breedGroupByPerformanceType = _list.stream()
               .collect(Collectors.groupingBy(PerformanceStatistics::getTypeKey));
         for (Map.Entry<Integer, List<PerformanceStatistics>> _breedByPerformanceType : _breedGroupByPerformanceType.entrySet()) {

            PerformanceTypeDetail _details = extractDetailPerformance(_breedByPerformanceType.getValue());
            
            _performances.put(TypePerformance.fromId(_breedByPerformanceType.getKey()), _details);
            
            enumType.removeIf(e -> e.equals(String.valueOf(TypePerformance.fromId(_breedByPerformanceType.getKey()))));
         }
         
         if (enumType.size()>0) 
            for (String s : enumType)
               _performances.put(TypePerformance.valueOf(s), emptyPerformanceTypeDetail());
                  
      } catch (Exception e) {
         logger.error("extractTypePerformance : {}",e.getMessage());
      } finally {
      }
      
      return _performances;
   }
   
   private PerformanceTypeDetail emptyPerformanceTypeDetail() {
      
      Map<TypeGender, Integer> gender = new HashMap<TypeGender, Integer>();
      gender.put(TypeGender.FATHER, 0);
      gender.put(TypeGender.MOTHER, 0);
      
      return new PerformanceTypeDetail()
            .withQtity(0)
            .withGender(gender)
            .withResults(null)
      ;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readYear(List<T> _stats, int _year) {
      
      Map<TypePerformance, PerformanceTypeDetail> _performances = null;
      List<PerformanceVariety> _variety = null;
      
      try {
         List<PerformanceStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         // Lecture des performances regroupées par type
         _performances = extractTypePerformance(_list);
   
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list, null);
      
      } catch (Exception e) {
         logger.error("readYear : {}",e.getMessage());
      } finally {
      }
      
      return (T) new PerformanceBreedStatistics()
            .withYear(_year)
            .withPerformances(_performances)
            .withVariety(_variety);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new PerformanceBreedStatistics()
            .withYear(_year)
            .withPerformances(extractTypePerformance(new ArrayList<PerformanceStatistics>()));

   }

   @Override
   protected <T> T readTopN(List<T> _stats, int _year) {
      return null;
   }

   @Override
   protected <T> T emptyTopN(int _year) {
      return null;
   }

   @Override
   @SuppressWarnings("unchecked")
   protected <T> T readBreed(List<T> _stats) {
      
      List<PerformanceBreedStatistics> _breedStatistics = null;
      
      try {

         List<PerformanceStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // On parcourt les années (on ajoute un tri)
         _breedStatistics = populateYears(_list);

      } catch (Exception e) {
         logger.error("readBreed : {}",e.getMessage());
      } finally {
      }
   
      // Création de l'objet Race
      return (T) new PerformanceBreed()
            .withId(this._idBreed)
            .withName(_nameBreed)
            .withStatistics(_breedStatistics);

   }
}
