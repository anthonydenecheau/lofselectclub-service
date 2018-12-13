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
import com.scc.lofselectclub.model.ConfirmationStatistics;
import com.scc.lofselectclub.model.GenericStatistics;
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.repository.ConfirmationRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.confirmation.ConfirmationBreedStatistics;
import com.scc.lofselectclub.template.confirmation.ConfirmationHeight;
import com.scc.lofselectclub.template.confirmation.ConfirmationHeightDetail;
import com.scc.lofselectclub.template.confirmation.ConfirmationVariety;
import com.scc.lofselectclub.template.confirmation.ConfirmationBreed;
import com.scc.lofselectclub.template.confirmation.ConfirmationResponseObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ConfirmationService extends AbstractGenericService<ConfirmationResponseObject,ConfirmationStatistics> {

   public ConfirmationService() {
      super();
      this.setGenericTemplate(new ConfirmationResponseObject());
      this.setType(ConfirmationStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(ConfirmationService.class);

   @Autowired
   private Tracer tracer;

   @Autowired
   private ConfirmationRepository confirmationRepository;

   @Autowired
   ServiceConfig config;

   /**
    * Retourne les données statistiques liées à la confirmation pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>ConfirmationResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackConfirmationList"
         , threadPoolKey = "getStatisticsConfirmation"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public ConfirmationResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the ConfirmationService.getStatistics() call, trace id: {}",
            tracer.getCurrentSpan().traceIdString());

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // Lecture des races associées au club pour lesquelles des données ont été calculées
         List<ConfirmationBreed> _breeds = populateBreeds(idClub);

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
    * @return        Objet <code>ConfirmationResponseObject</code>
    */
   private ConfirmationResponseObject buildFallbackConfirmationList(int idClub) {

      List<ConfirmationBreed> list = new ArrayList<ConfirmationBreed>();
      list.add(new ConfirmationBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {
   
      long _qtity = 0;
      ConfirmationHeight _height = null;
      
      try {
         List<ConfirmationStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
               
         // Somme des chiots males, femelles, portée
         _qtity = _list.stream()
               .collect(Collectors.counting());
         
         // lecture du détail par tailles
         _height = extractHeight(_list);

      } catch (Exception e) {
         logger.error("readVariety : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Variety
      return (T) new ConfirmationVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withQtity((int) (long) _qtity)
            .withHeight(_height);
            
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      return (T) new ConfirmationVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withQtity(0);
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M getDataStatistics(int idClub) {
      return 
            (M) confirmationRepository.findByIdClub(idClub)
            .stream()
            .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readYear(List<T> _stats, int _year) {
      
      long _qtity = 0;
      ConfirmationHeight _height = null;
      List<ConfirmationVariety> _variety = null;
      
      try {
         List<ConfirmationStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         // Nb de confirmations
         _qtity = _list.stream().collect(Collectors.counting());
   
         // lecture du détail par tailles
         _height = extractHeight(_list);
               
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list, null);
      
      } catch (Exception e) {
         logger.error("readYear : {}",e.getMessage());
      } finally {
      }
      
      return (T) new ConfirmationBreedStatistics()
            .withYear(_year)
            .withQtity((int) (long) _qtity)
            .withHeight(_height)
            .withVariety(_variety);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new ConfirmationBreedStatistics()
            .withYear(_year)
            .withQtity(0)
            .withVariety(populateVarieties(new ArrayList<ConfirmationStatistics>(), null));
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

      boolean _mandatoryHeight = false;  
      List<ConfirmationBreedStatistics> _breedStatistics = null;
      
      try {
         List<ConfirmationStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
   
         // Donnée taille est elle une donnée de confirmation
         String _height = _list
               .stream()
               .findFirst()
               .map(ConfirmationStatistics::getOnTailleObligatoire)
               .orElse("N");
         
         if ("O".equals(_height))
            _mandatoryHeight = true;
         
         // Lecture des années (on ajoute un tri)
         _breedStatistics = populateYears(_list);

      } catch (Exception e) {
         logger.error("readBreed : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Race
      return (T) new ConfirmationBreed()
            .withId(this._idBreed)
            .withName(this._nameBreed)
            .witMandatoryHeight(_mandatoryHeight)
            .withStatistics(_breedStatistics);
      
   }

   private ConfirmationHeight extractHeight(List<ConfirmationStatistics> _list) {

      List<ConfirmationHeightDetail> _details = new ArrayList<ConfirmationHeightDetail>();
      IntSummaryStatistics _summaryStats = null;
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      try {
         
         double _percent = 0;
         
         // Taille min, max, moyenne pour les races dont ce critère est obligatoire
         _summaryStats = _list.stream()
              .filter(x -> "O".equals(x.getOnTailleObligatoire()) && ( x.getTaille()!= null && x.getTaille()>0))
              .collect(Collectors.summarizingInt(ConfirmationStatistics::getTaille))
         ;
         
         int[] _serieHeight = IntStream.rangeClosed(_summaryStats.getMin(), _summaryStats.getMax()).toArray();
         for (int i = 0; i < _serieHeight.length; i++) {
            final int k = i;
            long _qtityBySerie = _list.stream()
                  .filter(x -> "O".equals(x.getOnTailleObligatoire()) && ( x.getTaille()!= null && x.getTaille() == _serieHeight[k] ))
                  .collect(Collectors.counting())
            ;

            _percent = Precision.round((double)_qtityBySerie / _summaryStats.getCount(), 2);
            
            _details.add(
                  new ConfirmationHeightDetail()
                     .withHeight(_serieHeight[k])
                     .withQtity((int)_qtityBySerie)
                     .withPercentage(format.format(_percent))
             );
              
         }
         
      } catch (Exception e) {
         logger.error("extractHeight : {}",e.getMessage());
      } finally {
      }
      
      return new ConfirmationHeight()
            .withQtity((int)_summaryStats.getCount())
            .withAvg((int) _summaryStats.getAverage())
            .withDetails(_details);
   }
   
}
