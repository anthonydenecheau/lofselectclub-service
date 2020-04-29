package com.scc.lofselectclub.services;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.scc.lofselectclub.template.registration.RegistrationBreed;
import com.scc.lofselectclub.template.registration.RegistrationBreedStatistics;
import com.scc.lofselectclub.template.registration.RegistrationResponseObject;
import com.scc.lofselectclub.template.registration.RegistrationVariety;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class RegistrationService extends AbstractGenericService<RegistrationResponseObject,BreederStatistics> {

   public RegistrationService() {
      super();
      this.setGenericTemplate(new RegistrationResponseObject());
      this.setType(BreederStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(RegistrationService.class);

   @Autowired
   protected BreederRepository breederRepository;

   @Autowired
   ServiceConfig config;

   /**
    * Retourne les données statistiques liées aux inscriptions pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>RegistrationResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackRegistrationList"
         , threadPoolKey = "getStatisticsRegistration"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public RegistrationResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      logger.debug("In the registrationService.getStatistics() call, trace id: {}", "[TODO]");

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // Lecture des races associées au club pour lesquelles des données ont été calculées
         List<RegistrationBreed> _breeds = populateBreeds(idClub);

         // Réponse
         return getGenericTemplate()
               .withBreeds(_breeds)
               .withSize(_breeds.size());

      } finally {
      }
   }

   /**
    * Fonction fallbackMethod de la fonction principale <code>getStatistics</code>
    * (Hystrix Latency / Fault Tolerance)
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>RegistrationResponseObject</code>
    */
   private RegistrationResponseObject buildFallbackRegistrationList(int idClub) {

      List<RegistrationBreed> list = new ArrayList<RegistrationBreed>();
      list.add(new RegistrationBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }


   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      BreederStatistics _sumRegistration = null;
      long _qtity = 0;
            
      try {
         // Caste la liste
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Somme des chiots males, femelles, portée
         _sumRegistration = _list
               .stream()
               .reduce(new BreederStatistics(0, 0),
                  (x, y) -> {
                     return new BreederStatistics(x.getNbMale() + y.getNbMale(), x.getNbFemelle() + y.getNbFemelle());
               });
   
         _qtity = _list
               .stream()
               .collect(Collectors.counting());
   
      } catch (Exception e) {
         logger.error("readVariety : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Variety
      return (T) new RegistrationVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withNumberOfMale(_sumRegistration.getNbMale())
            .withNumberOfFemale(_sumRegistration.getNbFemelle())
            .withNumberOfPuppies(_sumRegistration.getNbMale() + _sumRegistration.getNbFemelle())
            .withTotalOfLitter((int) (long) _qtity);

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      return (T) new RegistrationVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withNumberOfMale(0)
            .withNumberOfFemale(0)
            .withNumberOfPuppies(0)
            .withTotalOfLitter(0);
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

      BreederStatistics _sumRegistration = null; 
      long _qtity = 0;
      List<RegistrationVariety> _variety = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Somme des chiots males, femelles, portée
         _sumRegistration = _list
               .stream()
               .reduce(new BreederStatistics(0, 0),
                  (x, y) -> {
                     return new BreederStatistics(x.getNbMale() + y.getNbMale(),
                           x.getNbFemelle() + y.getNbFemelle());
               });
   
         _qtity = _stats
               .stream()
               .collect(Collectors.counting());
   
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list,null);

      } catch (Exception e) {
         logger.error("readYear : {}",e.getMessage());
      } finally {
      }
      
      return (T) new RegistrationBreedStatistics()
            .withYear(_year)
            .withNumberOfMale(_sumRegistration.getNbMale())
            .withNumberOfFemale(_sumRegistration.getNbFemelle())
            .withNumberOfPuppies(_sumRegistration.getNbMale() + _sumRegistration.getNbFemelle())
            .withTotalOfLitter((int) (long) _qtity)
            .withVariety(_variety);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new RegistrationBreedStatistics()
            .withYear(_year)
            .withNumberOfMale(0)
            .withNumberOfFemale(0)
            .withNumberOfPuppies(0)
            .withTotalOfLitter(0)
            .withVariety(populateVarieties(new ArrayList<BreederStatistics>(),null));
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

      List<RegistrationBreedStatistics> _breedStatistics = null;
      
      try { 
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Lecture des années (on ajoute un tri)
         _breedStatistics = populateYears(_list);
      
      } catch (Exception e) {
         logger.error("readBreed : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Race
      return (T) new RegistrationBreed()
            .withId(this._idBreed)
            .withName(this._nameBreed)
            .withStatistics(_breedStatistics);

   }

}
