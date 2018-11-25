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
import com.scc.lofselectclub.model.SerieCng;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityBreed;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityBreedStatistics;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityResponseObject;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityVariety;
import com.scc.lofselectclub.template.consanguinity.ConsanguintyCommonAncestor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ConsanguinityService extends AbstractGenericService<ConsanguinityResponseObject,BreederStatistics> {

   public ConsanguinityService() {
      super();
      this.setGenericTemplate(new ConsanguinityResponseObject());
      this.setType(BreederStatistics.class);
   }

   private static final Logger logger = LoggerFactory.getLogger(ConsanguinityService.class);

   @Autowired
   protected BreederRepository breederRepository;

   @Autowired
   private Tracer tracer;

   @Autowired
   ServiceConfig config;

   private double minVal = 0;
   private double maxVal = 0;
   private List<SerieCng> _serieCng = new ArrayList<SerieCng>();
   private final List<BreederStatistics> _emptyBreederStatistics = new ArrayList<BreederStatistics>();
   
   /**
    * Retourne les données statistiques liées à la consanguinité pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>ConsanguinityResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(commandKey = "lofselectclubservice"
         , fallbackMethod = "buildFallbackConsanguinityList"
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
   public ConsanguinityResponseObject getStatistics(int idClub) throws EntityNotFoundException {

      Span newSpan = tracer.createSpan("getStatistics");
      logger.debug("In the ConsanguinityService.getStatistics() call, trace id: {}",
            tracer.getCurrentSpan().traceIdString());

      try {

         // Deintition des plages Cng
         populateSeries();
         
         // Lecture des données races/variétés pour le club
         setClubBreedData(idClub);

         // Lecture des races associées au club pour lesquelles des données ont été calculées
         List<ConsanguinityBreed> _breeds = populateBreeds(idClub);
               
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
    * @return        Objet <code>ConsanguinityResponseObject</code>
    */
   private ConsanguinityResponseObject buildFallbackConsanguinityList(int idClub) {

      List<ConsanguinityBreed> list = new ArrayList<ConsanguinityBreed>();
      list.add(new ConsanguinityBreed().withId(0));
      return getGenericTemplate().withBreeds(list).withSize(list.size());
   }
 
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);
      
      // Caste la liste
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Moyenne des coef. de consanguinité
      double _cng = _list.stream()
            .mapToDouble(BreederStatistics::getConsanguinite)
            .average()
            .orElse(0.0);
      
      // Répartition du nb de chiots pour les plages Cng
      List<Map<String, Object>> _series = extractSeries(_serieCng, _list);
      
      // Création de l'objet Variety
      return (T) new ConsanguinityVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withCng(format.format(Precision.round(_cng, 4)))
            .withSeries(_series);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return (T) new ConsanguinityVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withCng(format.format((double) 0))
            .withSeries(extractSeries(this._serieCng, this._emptyBreederStatistics));
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

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      format.setMaximumFractionDigits(2);
      format.setMinimumFractionDigits(2);

      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Moyenne des coef. de consanguinité
      double _cng = _list.stream()
            .mapToDouble(BreederStatistics::getConsanguinite)
            .average().orElse(0.0);

      // Répartition du nb de chiots pour les plages Cng
      List<Map<String, Object>> _series = extractSeries(_serieCng, _list);
      
      // Lecture des variétés s/ la race en cours (et pour l'année en cours)
      List<ConsanguinityVariety> _variety = populateVarieties(_list, null);

      return (T) new ConsanguinityBreedStatistics()
            .withYear(_year)
            .withCng(format.format(Precision.round(_cng, 4)))
            .withSeries(_series)
            .withVariety(_variety);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return (T) new ConsanguinityBreedStatistics().withYear(_year)
         .withCng(format.format((double) 0))
         .withSeries(extractSeries(this._serieCng, this._emptyBreederStatistics))
         .withVariety(populateVarieties(new ArrayList<BreederStatistics>(), null));
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

      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Lecture des années (on ajoute un tri)
      List<ConsanguinityBreedStatistics> _breedStatistics = populateYears(_list);

      // Création de l'objet Race
      return (T) new ConsanguinityBreed()
            .withId(this._idBreed)
            .withName(this._nameBreed)
            .withStatistics(_breedStatistics);

   }

   /**
    * Construction des plages Cng
    */
   private void populateSeries() {
      
      _serieCng.clear();
      _serieCng.add(new SerieCng(0d, null, "0",1));
      _serieCng.add(new SerieCng(0d, 0.03125d, "0 - 3.125",2));
      _serieCng.add(new SerieCng(0.03125d, 0.0625d, "3.125 - 6.25",3));
      _serieCng.add(new SerieCng(0.0625d, 0.125d, "6.25 - 12.5",4));
      _serieCng.add(new SerieCng(0.125d, 0.25d, "12.5 - 25",5));

   
   }
   
   /**
    * Retourne le nombre de chiots à naitre ayant un coef. Cng appartenant à la série
    * 
    * @param _plages Liste des séries Cng
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>series</code> des objets <code>ConsanguinityBreedStatistics</code>, <code>ConsanguinityBreedStatistics</code> et <code>ConsanguinityVariety</code>
    */
   private List<Map<String, Object>> extractSeries(List<SerieCng> _plages, List<BreederStatistics> _list) {

      List<Map<String, Object>> _series = new ArrayList<Map<String, Object>>();

      // Par tranche
      for (SerieCng plage : _plages) {

         Map<String, Object> _serie = new HashMap<String, Object>();

         minVal = (plage.getMinValue() == null ? 0 : plage.getMinValue());
         maxVal = (plage.getMaxValue() == null ? 0 : plage.getMaxValue());

         BreederStatistics sumBirth = _list
               .stream()
               .filter(e -> matchesRange(e.getConsanguinite(), minVal, maxVal))
               .reduce(new BreederStatistics(0, 0),
                     (x, y) -> {
                        return new BreederStatistics(x.getNbMale() + y.getNbMale(), x.getNbFemelle() + y.getNbFemelle());
                  })
         ;
         
         _serie.put("serie", plage.getLibelle());
         _serie.put("qtity", sumBirth.getNbMale()+sumBirth.getNbFemelle());
         _series.add(new HashMap<String, Object>(_serie));

      }
      return _series;
   }

   /**
    * Détermine si les chiots appartiennent à la série lue
    * 
    * @param e          Cng de la portée
    * @param range_min  Limite inférieure de la série
    * @param range_max  Limite supérieure de la série
    * @return           <code>true</code> si la portée appartient à la série
    */
   private boolean matchesRange(double e, double range_min, double range_max) {

      if (range_min == 0 && range_max == 0)
         // e and range_min are equals
         if (Double.compare(e,range_min)==0)
            return true;
      
      if (range_min >= 0 && range_max > 0)
         // e is greater than range_min and e is lower or equals than range_max
         if (Double.compare(e,range_min)>0 && Double.compare(e,range_max)<=0 )
            return true;

      return false;
   }
   
}
