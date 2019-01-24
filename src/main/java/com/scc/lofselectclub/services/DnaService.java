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
import com.scc.lofselectclub.model.DnaStatistics;
import com.scc.lofselectclub.model.GenericStatistics;
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.repository.DnaRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.dna.DnaBreedStatistics;
import com.scc.lofselectclub.template.dna.DnaVariety;
import com.scc.lofselectclub.template.dna.DnaBreed;
import com.scc.lofselectclub.template.dna.DnaResponseObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class DnaService extends AbstractGenericService<DnaResponseObject,DnaStatistics> {

   public DnaService() {
      super();
      this.setGenericTemplate(new DnaResponseObject());
      this.setType(DnaStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(DnaService.class);

   @Autowired
   private DnaRepository dnaRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;

   /**
    * Retourne les données statistiques liées à l'ADN pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>DnaResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackDnaList"
         , threadPoolKey = "getStatisticsDna"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public DnaResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the DnaService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // On parcourt la liste des races associées au club pour lesquelles des données ont été calculées
         List<DnaBreed> _breeds = populateBreeds(idClub);
               
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
    * @return        Objet <code>DnaResponseObject</code>
    */
   private DnaResponseObject buildFallbackDnaList(int idClub) {

      List<DnaBreed> list = new ArrayList<DnaBreed>();
      list.add(new DnaBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      int _qtity = 0;
      DnaStatistics sumDna = null; 
            
      try { 
         // Caste la liste
         List<DnaStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         // Somme des resultats Adn
         sumDna = _list.stream()
               .reduce(new DnaStatistics(0, 0, 0, 0), (x, y) -> {
                           return new DnaStatistics(
                                 x.getDna() + y.getDna()
                                 , x.getDnaComp() + y.getDnaComp()
                                 , x.getDnaCompP() + y.getDnaCompP()
                                 , x.getDnaCompM() + y.getDnaCompM());
         });
         
         _qtity = sumDna.getDnaComp()+sumDna.getDnaCompP()+sumDna.getDnaCompM();
      
      } catch (Exception e) {
         logger.error("readVariety : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Variety
      return (T) new DnaVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withQtity(_qtity)
            .withDna(sumDna.getDna())
            .withDnaComp(sumDna.getDnaComp())
            .withDnaCompP(sumDna.getDnaCompP())
            .withDnaCompM(sumDna.getDnaCompM());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      return (T) new DnaVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withQtity(0)
            .withDna(0)
            .withDnaComp(0)
            .withDnaCompP(0)
            .withDnaCompM(0);
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M getDataStatistics(int idClub) {
      return 
            (M) dnaRepository.findByIdClub(idClub, orderByTri())
            .stream()
            .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readYear(List<T> _stats, int _year) {

      int _qtity = 0; 
      DnaStatistics sumDna = null;
      List<DnaVariety> _variety = null;
      
      try {
         List<DnaStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Somme des resultats Adn
         sumDna = _list.stream().reduce(new DnaStatistics(0, 0, 0, 0),
               (x, y) -> {
                  return new DnaStatistics(
                        x.getDna() + y.getDna()
                        , x.getDnaComp() + y.getDnaComp()
                        , x.getDnaCompP() + y.getDnaCompP()
                        , x.getDnaCompM() + y.getDnaCompM());
               });
         
         _qtity = sumDna.getDnaComp()+sumDna.getDnaCompP()+sumDna.getDnaCompM();
         
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list, null);
      
      } catch (Exception e) {
         logger.error("readYear : {}",e.getMessage());
      } finally {
      }
      
      return (T) new DnaBreedStatistics()
            .withYear(_year)
            .withQtity(_qtity)
            .withDna(sumDna.getDna())
            .withDnaComp(sumDna.getDnaComp())
            .withDnaCompP(sumDna.getDnaCompP())
            .withDnaCompM(sumDna.getDnaCompM())
            .withVariety(_variety);

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new DnaBreedStatistics()
            .withYear(_year)
            .withQtity(0)
            .withDna(0)
            .withDnaComp(0)
            .withDnaCompP(0)
            .withDnaCompM(0)
            .withVariety(populateVarieties(new ArrayList<DnaStatistics>(), null));
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

      List<DnaBreedStatistics> _breedStatistics = null;
      
      try {
         List<DnaStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // On parcourt les années (on ajoute un tri)
         _breedStatistics = populateYears(_list);

      } catch (Exception e) {
         logger.error("readBreed : {}",e.getMessage());
      } finally {
      }
   
      // Création de l'objet Race
      return (T) new DnaBreed()
            .withId(this._idBreed)
            .withName(_nameBreed)
            .withStatistics(_breedStatistics);

   }

}
