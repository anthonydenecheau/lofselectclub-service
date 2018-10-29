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
import com.scc.lofselectclub.template.consanguinity.ConsanguinityBreed;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityBreedStatistics;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityResponseObject;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityVariety;
import com.scc.lofselectclub.template.consanguinity.ConsanguintyCommonAncestor;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
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

   /**
    * Retourne les données statistiques liées à la consanguinité pour l'ensemble des races affiliées au club
    * 
    * @param idClub  Identifiant du club
    * @return        Objet <code>ConsanguinityResponseObject</code>
    * @throws EntityNotFoundException
    */
   @HystrixCommand(fallbackMethod = "buildFallbackConsanguinityList", threadPoolKey = "getStatistics", threadPoolProperties = {
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

   /**
    * Retourne le nombre de portées ayant n ancêtres communs
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>litterByCommonAncestor</code> de l'objet <code>ConsanguinityBreedStatistics</code>
    */
   private List<ConsanguintyCommonAncestor> extractCommonAncestors(List<BreederStatistics> _list) {

      List<ConsanguintyCommonAncestor> _commonAncestors = new ArrayList<ConsanguintyCommonAncestor>();

      Map<Integer, Integer> _breedAndCommonAncestor = _list.stream()
            .collect(Collectors.groupingBy(BreederStatistics::getNbAncetreCommun
                        , Collectors.collectingAndThen(
                                Collectors.mapping(BreederStatistics::getIdSaillie, Collectors.toSet()), Set::size)));

      // Remarque : la liste _breedAndCommonAncestor contient nombre d'ancêtres
      // communs, la liste des dossier
      // il faut maintenant compter de min à max (nb d'ancêtres commun), le nombre de
      // dossiers
      SortedMap<Integer, Integer> _series = new TreeMap<Integer, Integer>(_breedAndCommonAncestor);

      Integer highestKey = _series.lastKey();
      // Integer lowestKey = _series.firstKey();

      if (_series.size() > 0)
         for (int i = 1; i <= highestKey; i++) {
            ConsanguintyCommonAncestor c = null;
            if (_series.containsKey(i))
               c = new ConsanguintyCommonAncestor().withNumberOfCommonAncestor(i).withNumberOfLitter(_series.get(i));
            else
               c = new ConsanguintyCommonAncestor().withNumberOfCommonAncestor(i).withNumberOfLitter(0);

            _commonAncestors.add(c);
         }

      return _commonAncestors;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   protected <T> T readVariety(List<T> _stats, ParametersVariety _parameters) {

      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      // Caste la liste
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Moyenne des coef. de consanguinité
      double _cng = _list.stream()
            .mapToDouble(BreederStatistics::getConsanguinite)
            .average()
            .orElse(0.0);

      // Moyenne du nb d'ancètres communs
      List<ConsanguintyCommonAncestor> _commonAncestors = extractCommonAncestors(_list);

      // Création de l'objet Variety
      return (T) new ConsanguinityVariety()
            .withId(this._idVariety)
            .withName(this._nameVariety)
            .withCng(format.format(Precision.round(_cng, 2)))
            .withLitterByCommonAncestor(_commonAncestors);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters) {
      
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return (T) new ConsanguinityVariety()
            .withId(_variety.getId())
            .withName(_variety.getName())
            .withCng(format.format((double) 0))
            .withLitterByCommonAncestor(new ArrayList<ConsanguintyCommonAncestor>());
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
      List<BreederStatistics> _list = feed((List<? extends GenericStatistics>) _stats);
      
      // Moyenne des coef. de consanguinité
      double _cng = _list.stream()
            .mapToDouble(BreederStatistics::getConsanguinite)
            .average().orElse(0.0);

      // Nb portées par nb d'ancètres communs
      List<ConsanguintyCommonAncestor> _commonAncestors = extractCommonAncestors(_list);

      // Lecture des variétés s/ la race en cours (et pour l'année en cours)
      List<ConsanguinityVariety> _variety = populateVarieties(_list, null);

      return (T) new ConsanguinityBreedStatistics()
            .withYear(_year)
            .withCng(format.format(Precision.round(_cng, 2)))
            .withLitterByCommonAncestor(_commonAncestors)
            .withVariety(_variety);
      
   }

   @SuppressWarnings("unchecked")
   @Override
   protected <T> T emptyYear(int _year) {
      NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
      
      return (T) new ConsanguinityBreedStatistics().withYear(_year)
         .withCng(format.format((double) 0))
         .withLitterByCommonAncestor(new ArrayList<ConsanguintyCommonAncestor>())
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
   
}
