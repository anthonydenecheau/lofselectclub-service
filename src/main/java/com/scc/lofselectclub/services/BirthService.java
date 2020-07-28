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
import com.scc.lofselectclub.template.birth.BirthBreed;
import com.scc.lofselectclub.template.birth.BirthResponseObject;
import com.scc.lofselectclub.template.birth.BirthVariety;
import com.scc.lofselectclub.template.birth.BirthBreedStatistics;
import com.scc.lofselectclub.template.birth.BirthCotation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class BirthService extends AbstractGenericService<BirthResponseObject,BreederStatistics> {

   public BirthService() {
      super();
      this.setGenericTemplate(new BirthResponseObject());
      this.setType(BreederStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(BirthService.class);

   @Autowired
   protected BreederRepository breederRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;

   /**
    * Retourne les données statistiques liées aux naissances pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>BirthResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackBirthList"
         , threadPoolKey = "getStatisticsBirth"
         , threadPoolProperties = {
         @HystrixProperty(name = "coreSize", value = "30"),
         @HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
               @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
               @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
               @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
               @HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
               @HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
                     EntityNotFoundException.class })
   public BirthResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the birthService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

      try {

         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // Lecture des races associées au club pour lesquelles des données ont été calculées
         List<BirthBreed> _breeds = populateBreeds(idClub);

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
    * @return        Objet <code>BirthResponseObject</code>
    */
   private BirthResponseObject buildFallbackBirthList(int idClub) {

      List<BirthBreed> list = new ArrayList<BirthBreed>();
      list.add(new BirthBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }

   /**
    * Retourne la répartition du nombre de portées par cotation
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>cotations</code> de l'objet <code>BirthBreedStatistics</code>
    */
   private List<BirthCotation> extractCotation(List<BreederStatistics> _list) {

      List<BirthCotation> _cotationList = new ArrayList<BirthCotation>();
      int[] _cotReferences = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);
      
      try {
         Map<Integer, Long> _cotations = _list
               .stream()
               .collect(Collectors.groupingBy(BreederStatistics::getCotationPortee, Collectors.counting()));
   
         double _total = _cotations.values()
               .stream()
               .mapToInt(Number::intValue)
               .sum();
         
         double _percent = 0;
         double _arrondi = 0;
         
         for (Map.Entry<Integer, Long> _cot : _cotations.entrySet()) {
   
            _percent = Precision.round((double) _cot.getValue() / _total, 4);
            _arrondi += _percent; 
            
            // Suppression de la cotation traitée
            _cotReferences = ArrayUtils.removeElement(_cotReferences, _cot.getKey());
            BirthCotation c = new BirthCotation()
                  .withGrade(_cot.getKey())
                  .withQtity((int) (long) _cot.getValue())
                  .withPercentage(format.format(_percent));
            _cotationList.add(c);
         }
         
         // Gestion des arrondis
         if (_cotationList.size() > 1) {
            if (_arrondi != (double)1) {
               Stream<BirthCotation> stream = _cotationList.stream();
               stream.reduce((first, second) -> second)
                  .orElse(null)
                  .setPercentage(format.format(Precision.round(_percent,4)+Precision.round((1-_arrondi),4)));
            }
         }
   
         for (int i : _cotReferences) {
            BirthCotation c = new BirthCotation()
                  .withGrade(i)
                  .withQtity(0)
                  .withPercentage(format.format(0));
            _cotationList.add(c);
         }
   
         _cotationList.sort(Comparator.comparing(BirthCotation::getGrade));

      } catch (Exception e) {
         logger.error("extractCotation : {}",e.getMessage());
      } finally {
      }
      
      return _cotationList;
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      BreederStatistics _sumBirth = null;
      long _qtity = 0;
      double _prolificity = 0d;
      List<BirthCotation> _cotations = null; 
            
      try {
         // Caste la liste
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Somme des chiots males, femelles, portée
         _sumBirth = _list
               .stream()
               .reduce(new BreederStatistics(0, 0),
                  (x, y) -> {
                     return new BreederStatistics(x.getNbMale() + y.getNbMale(), x.getNbFemelle() + y.getNbFemelle());
               });
   
         _qtity = _list
               .stream()
               .collect(Collectors.counting());
   
         _prolificity = _list
               .stream()
               .findFirst()
               .map(BreederStatistics::getProlificiteVariete)
               .orElse(0.0);
   
         // Lecture des cotations des portée
         _cotations = extractCotation(_list);

      } catch (Exception e) {
         logger.error("readVariety : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Variety
      return (T) new BirthVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withNumberOfMale(_sumBirth.getNbMale())
            .withNumberOfFemale(_sumBirth.getNbFemelle())
            .withNumberOfPuppies(_sumBirth.getNbMale() + _sumBirth.getNbFemelle())
            .withTotalOfLitter((int) (long) _qtity)
            .withProlificity(Precision.round(_prolificity, 2))
            .withCotations(_cotations);

   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      return (T) new BirthVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withNumberOfMale(0)
            .withNumberOfFemale(0)
            .withNumberOfPuppies(0)
            .withTotalOfLitter(0)
            .withProlificity(0)
            .withCotations(extractCotation(new ArrayList<BreederStatistics>()));
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

      BreederStatistics _sumBirth = null; 
      long _qtity = 0;
      double _prolificity = 0d;
      List<BirthCotation> _cotations = null; 
      List<BirthVariety> _variety = null;
      
      try {
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Somme des chiots males, femelles, portée
         _sumBirth = _list
               .stream()
               .reduce(new BreederStatistics(0, 0),
                  (x, y) -> {
                     return new BreederStatistics(x.getNbMale() + y.getNbMale(),
                           x.getNbFemelle() + y.getNbFemelle());
               });
   
         _qtity = _stats
               .stream()
               .collect(Collectors.counting());
   
         _prolificity = _list
               .stream()
               .findFirst()
               .map(BreederStatistics::getProlificiteRace)
               .orElse(0.0);
   
         // Lecture des cotations des portées s/ la race en cours (et pour l'année en cours)
         _cotations = extractCotation(_list);
   
         // Lecture des variétés s/ la race en cours (et pour l'année en cours)
         _variety = populateVarieties(_list,null);

      } catch (Exception e) {
         logger.error("readYear : {}",e.getMessage());
      } finally {
      }
      
      return (T) new BirthBreedStatistics()
            .withYear(_year)
            .withNumberOfMale(_sumBirth.getNbMale())
            .withNumberOfFemale(_sumBirth.getNbFemelle())
            .withNumberOfPuppies(_sumBirth.getNbMale() + _sumBirth.getNbFemelle())
            .withTotalOfLitter((int) (long) _qtity)
            .withProlificity(Precision.round(_prolificity, 2))
            .withVariety(_variety)
            .withCotations(_cotations);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      return (T) new BirthBreedStatistics()
            .withYear(_year)
            .withNumberOfMale(0)
            .withNumberOfFemale(0)
            .withNumberOfPuppies(0)
            .withTotalOfLitter(0)
            .withProlificity(0)
            .withVariety(populateVarieties(new ArrayList<BreederStatistics>(),null))
            .withCotations(extractCotation(new ArrayList<BreederStatistics>()));
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

      List<BirthBreedStatistics> _breedStatistics = null;
      
      try { 
         List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
         
         // Lecture des années (on ajoute un tri)
         _breedStatistics = populateYears(_list);
      
      } catch (Exception e) {
         logger.error("readBreed : {}",e.getMessage());
      } finally {
      }
      
      // Création de l'objet Race
      return (T) new BirthBreed()
            .withId(this._idBreed)
            .withName(this._nameBreed)
            .withStatistics(_breedStatistics);

   }

   @Override
   protected <T> T readTopOfTheYear(List<T> _stats, int _year) {
      return null;
   }

   @Override
   protected <T> T emptyTopOfTheYear(int _year) {
      return null;
   }

}
