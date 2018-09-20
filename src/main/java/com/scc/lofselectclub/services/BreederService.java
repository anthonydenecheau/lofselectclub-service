package com.scc.lofselectclub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scc.lofselectclub.exceptions.EntityNotFoundException;
import com.scc.lofselectclub.config.ServiceConfig;
import com.scc.lofselectclub.model.BreederStatistics;
import com.scc.lofselectclub.model.SerieDefinition;
import com.scc.lofselectclub.model.ConfigurationRace;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.repository.SerieDefinitionRepository;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.breeder.BreederMonthStatistics;
import com.scc.lofselectclub.template.breeder.BreederAffixStatistics;
import com.scc.lofselectclub.template.breeder.BreederAffixVariety;
import com.scc.lofselectclub.template.breeder.BreederBreed;
import com.scc.lofselectclub.template.breeder.BreederResponseObject;
import com.scc.lofselectclub.template.breeder.BreederBreedStatistics;
import com.scc.lofselectclub.template.breeder.BreederVarietyStatistics;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.breeder.BreederVariety;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class BreederService {

    private static final Logger logger = LoggerFactory.getLogger(BreederService.class);
    
    @Autowired
    private Tracer tracer;

    @Autowired
    private BreederRepository breederRepository;
    
    @Autowired
    private ConfigurationRaceRepository configurationRaceRepository;

    @Autowired
    private SerieDefinitionRepository rangeDefinitionRepository;

    @Autowired
    ServiceConfig config;

    private Integer minVal = 0;
    private Integer maxVal = 0;
    int limitTopN = 0;
    
    private Set<String> allTopN = new HashSet<String>();

	@HystrixCommand(fallbackMethod = "buildFallbackBreederList",
            threadPoolKey = "getStatistics",
            threadPoolProperties =
                    {@HystrixProperty(name = "coreSize",value="30"),
                     @HystrixProperty(name="maxQueueSize", value="10")},
            commandProperties={
                     @HystrixProperty(name="circuitBreaker.requestVolumeThreshold", value="10"),
                     @HystrixProperty(name="circuitBreaker.errorThresholdPercentage", value="75"),
                     @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds", value="7000"),
                     @HystrixProperty(name="metrics.rollingStats.timeInMilliseconds", value="15000"),
                     @HystrixProperty(name="metrics.rollingStats.numBuckets", value="5")},
            ignoreExceptions= { EntityNotFoundException.class}
    )
    public BreederResponseObject getStatistics(int idClub) throws EntityNotFoundException {

        Span newSpan = tracer.createSpan("getStatistics");
        logger.debug("In the breederService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

        // topN affixe
        limitTopN = config.getLimitTopNAffix();

        int _id = 0;
        String _name = "";
        String period = "";

        List<BreederBreed> _breeds = new ArrayList<BreederBreed>();
        
        try {
        	
            // Lecture des races associées au club qui ont une production 
        	// TODO : QUID des races pour le club qui n'ont pas eu de production s/ les 5 dernieres années ?
            Map<TupleBreed,List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub)
    			.stream()
    			.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())))
    		;
            
            // Exception si le club n'a pas de races connues
            if (_allBreeds.size() == 0)
            	throw new EntityNotFoundException(BreederResponseObject.class, "idClub", String.valueOf(idClub));
            
            for (Map.Entry<TupleBreed,List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {

            	List<BreederAffixStatistics> _affixesStatistics = new ArrayList<BreederAffixStatistics>();  
            	List<BreederBreedStatistics> _breedStatistics = new ArrayList<BreederBreedStatistics>();  		
            	int _year = 0;
            	int _qtity = 0;
            	
            	_id = _currentBreed.getKey().getId();
            	_name = _currentBreed.getKey().getName();

            	// Recherche TopN Affixe s/ la race
            	List<BreederStatistics> _topsNAffixes = extractTopNAffixes(_currentBreed.getValue());

                // Lecture des plages paramétrées pour la race en cours
            	// Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
            	// A voir si les années précédentes ne feront pas l'objet d'une suppression côté data (BdD); auquel cas, ce code sera obsolète 
                ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(_id);
                List<SerieDefinition> plages = rangeDefinitionRepository.findByIdSerieGroupOrderBySequence(_configurationRace.getIdSerieGroup());
            	int [] _serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
            	final int minYear =_serieYear[0];
                period = _configurationRace.getBreakPeriod();

            	// TODO : Quid des années pour lesquels aucune production n'a été enregistrée

            	// Lecture des années (on ajoute un tri) 
            	Map<Integer,List<BreederStatistics>> _breedGroupByYear= _currentBreed.getValue()
            			.stream()
            			.filter(x -> x.getAnnee() >= minYear)
            			.collect(StreamUtils.sortedGroupingBy(BreederStatistics::getAnnee))
            	;
            	for (Map.Entry<Integer,List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

                	List<BreederMonthStatistics> _months = new ArrayList<BreederMonthStatistics>();  		
                	_year = _breedOverYear.getKey();
                	
                	// Suppression de l'année traitée
                	_serieYear=ArrayUtils.removeElement(_serieYear,_year); 
                	
                	// Total de la production de l'année en cours
                	_qtity = sumLitter(_breedOverYear.getValue());
                	
                	// Recherche Production de l'année en cours pour les plages paramétrées s/ la race
                	List<Map<String, Object>> _series = extractSeries(plages, _breedOverYear.getValue());
            		
            		// Lecture des variétés s/ la race en cours (et pour l'année en cours)
                	List<BreederVariety> _variety = extractVariety(plages, _breedOverYear.getValue());

                	// TODO : Quid des mois pour lesquels aucune production n'a été enregistrée
                	if ("MM".equals(period)) {
                		
                		int _month = 0;
                		int _monthQtity = 0;
                		int[] _listMonths = new int[] {1,2,3,4,5,6,7,8,9,10,11,12};
                		
                		// On complète les données mensuelles (on ajoute un tri == permet de détecter les ruptures dans la production)
                    	Map<Integer,List<BreederStatistics>> _breedGroupByMonth = _breedOverYear.getValue()
                    			.stream()
                    			.collect(StreamUtils.sortedGroupingBy(BreederStatistics::getMois))
                    	;
                    	for (Map.Entry<Integer,List<BreederStatistics>> _breedOverMonth : _breedGroupByMonth.entrySet()) {

                    		_month = _breedOverMonth.getKey();
                        		
                    		// Suppression du mois traité
                    		_listMonths=ArrayUtils.removeElement(_listMonths,_month); 
                    			
                        	// Total de la production du mois en cours
                    		_monthQtity = sumLitter(_breedOverMonth.getValue());
                        	
                        	// Recherche Production du mois en cours pour les plages paramétrées s/ la race
                        	List<Map<String, Object>> _seriesMonth = extractSeries(plages, _breedOverMonth.getValue());
                    		
                        	// Lecture des variétés s/ la race en cours (et pour le mois en cours)
                        	List<BreederVariety> _varietyMonth = extractVariety(plages, _breedOverMonth.getValue());

                        	BreederMonthStatistics _monthStatitics = new BreederMonthStatistics()
                        			.withMonth(_month)
                        			.withQtity(_monthQtity)
                        			.withSeries(_seriesMonth)
                        			.withVariety(_varietyMonth)
                        	;
                        	
                        	_months.add(_monthStatitics);
                    	}
                    	
                    	// Complète les infos s/ les mois manquants
                    	if (_listMonths.length>0)
                    		for (int i = 0; i < _listMonths.length; i++) {
                    			//System.out.print(" -> "+_listMonths[i]);
                    			_months.add( 
                    				new BreederMonthStatistics()
                    					.withMonth(_listMonths[i])
                    					.withQtity(0)
                    					.withSeries(new ArrayList<Map<String, Object>>())
                    					.withVariety(new ArrayList<BreederVariety>())                    			
                        		);
                    		}
                    	
                    	// Mise à jour == Tri s/ les mois
                    	_months.sort(Comparator.comparing(BreederMonthStatistics::getMonth));
                	}
                	
                	// Création de l'objet Statistique de l'année en cours pour la race (inclus variété)
                	BreederBreedStatistics _breed = new BreederBreedStatistics()
            			.withYear(_year)
            			.withQtity(_qtity)
            			.withSeries(_series)
            			.withVariety(_variety)
            			.withMonths(_months)
            		;
                	_breedStatistics.add(_breed);

                	// Recherche TopN Affixe de l'année en cours s/ la race et sur les varietes
            		List<Map<String, Object>> _topsN = extractTopNOverYear(_year,_topsNAffixes);

            		// Lecture TopN Affixe par variétés s/ la race en cours (et pour l'année en cours)
                	List<BreederAffixVariety> _topNVariety = extractTopNVariety(_year, _topsNAffixes);

                	BreederAffixStatistics _affixStatistics = new BreederAffixStatistics()
                			.withYear(_year)
                			.withAffixes(_topsN)
                			.withVariety(_topNVariety)
                	;
                	_affixesStatistics.add(_affixStatistics);

            	}
            	
            	// On finalise en initialisant les années pour lesquelles on a constaté une rupture
            	for (int i = 0; i < _serieYear.length; i++) {
            		BreederBreedStatistics _breed = new BreederBreedStatistics()
            			.withYear(_serieYear[i])
            			.withQtity(0)
            			.withSeries(new ArrayList<Map<String, Object>>())
            			.withVariety(new ArrayList<BreederVariety>())
            			.withMonths(new ArrayList<BreederMonthStatistics>())
            		;	
            		_breedStatistics.add(_breed);
            		
            		BreederAffixStatistics _affixStatistics = new BreederAffixStatistics()
                			.withYear(_serieYear[i])
                			.withAffixes(new ArrayList<Map<String, Object>>())
                			.withVariety(new ArrayList<BreederAffixVariety>())
                	;
                	_affixesStatistics.add(_affixStatistics);
            	}
            	
            	// Création de l'objet Race
            	BreederBreed _breed = new BreederBreed()
            		.withId(_id)
            		.withName(_name)
            		.withStatistics(_breedStatistics)
            		.withTopN(_affixesStatistics)
            	;
            	
            	// Ajout à la liste 
            	_breeds.add(_breed);
            }
           
            // Réponse
        	return new BreederResponseObject()
            		.withBreeds(_breeds)
            		.withSize(_breeds.size())
            ;
            
        	
        }
	    finally{
	    	newSpan.tag("peer.service", "postgres");
	        newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
	        tracer.close(newSpan);
	    }
    }

	private BreederResponseObject buildFallbackBreederList(int idClub){
    	
    	List<BreederBreed> list = new ArrayList<BreederBreed>(); 
    	list.add(new BreederBreed()
                .withId(0))
    	;
        return new BreederResponseObject(list.size(),list);
    }
    
	private List<BreederVariety> extractVariety(List<SerieDefinition> plages, List<BreederStatistics> _list) {

		List<BreederVariety> _varietyList = new ArrayList<BreederVariety>(); 
		int _id = 0;
		int _qtity = 0;
		String _name = "";
		
		Map<TupleVariety,List<BreederStatistics>> _allVariety = _list
				.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())))
		;
		
		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if ( _allVariety.size() == 1 
				&& StreamUtils.breedMonoVariety(_allVariety.keySet().stream().findFirst().get().getName()) ) 
			return _varietyList;
		
		for (Map.Entry<TupleVariety,List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {
			
			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

        	// Total de la production de l'année en cours
			_qtity = sumLitter(_currentVariety.getValue());
    		
			// Recherche Production pour les plages paramétrées s/ la variété en cours
			List<Map<String, Object>> _series = extractSeries(plages, _currentVariety.getValue());

			// Création de l'objet VarietyStatistics
			BreederVarietyStatistics _varietyStatistics = new BreederVarietyStatistics()
					.withQtity(_qtity)
					.withSeries(_series)
			;

			// Création de l'objet Variety
			BreederVariety _variety = new BreederVariety()
					.withId(_id)
					.withName(_name)
					.withStatistics(_varietyStatistics)
			;		
			_varietyList.add(_variety);
		}
		return _varietyList;
	}

	private List<Map<String, Object>> extractSeries(List<SerieDefinition> plages, List<BreederStatistics> _list) {
		
    	List<Map<String, Object>> _series = new ArrayList<Map<String, Object>>();

		// Par tranche
		for (SerieDefinition plage : plages) {
			
			Map<String, Object> _serie = new HashMap<String, Object>();
			
			minVal = (plage.getMinValue() == null ? 0 : plage.getMinValue());
			maxVal = (plage.getMaxValue() == null ? 0 : plage.getMaxValue());
			
			Set<Integer> units = _list
					.stream()
					.collect(
							Collectors.collectingAndThen(
									Collectors.groupingBy(BreederStatistics::getIdEleveur, Collectors.counting()), 
									(map) -> map.entrySet()
										.stream()
										.filter(e -> matchesRange(e,minVal,maxVal))
										.map(e -> e.getKey())
										.collect(Collectors.toSet())
									)
					);    
			
			_serie.put("serie", plage.getLibelle());
			_serie.put("qtity", units.size());
			_series.add(new HashMap<String, Object>(_serie));
			
		}
		return _series;
	}

	private List<Map<String, Object>> extractTopNOverYear (int _year, List<BreederStatistics> _list) {

		List<Map<String, Object>> _topsN = new ArrayList<Map<String, Object>>();

		try {
			
			// 1. On groupe les affixes par qtites pour l'année en cours
			Map<String, Long> _affixes = _list
					.stream()
					.filter(x -> ( _year == x.getAnnee()) )
					.collect(
			                Collectors.groupingBy(
			                		BreederStatistics::getAffixeEleveur, Collectors.counting()
			                )
			        );
			;
	
			// 3. On complète par les affixes potentiellement manquants
			boolean g = false;
			for (String s : this.allTopN) {
				g = false;
	            for (Map.Entry<String, Long> entry : _affixes.entrySet()) {
	                if ( s.equals(entry.getKey())) {
	                	g = true;
	                	break;
	                }
	            }
				
				if (!g){
					Map<String, Object> _topN = new HashMap<String, Object>(); 
					_topN.put("name", s);
					_topN.put("qtity", (long) 0);
					_topsN.add(new HashMap<String, Object>(_topN));
				}	
			}
			
			// 2. On alimente notre Map
			for (Entry<String, Long> _affixe : _affixes.entrySet()) {
				Map<String, Object> _topN = new HashMap<String, Object>();            		
				_topN.put("name", _affixe.getKey());
				_topN.put("qtity", _affixe.getValue());
				_topsN.add(new HashMap<String, Object>(_topN));
			}
	
			// 4. On trie les résultats par quantites décroissante
			_topsN.sort(Collections.reverseOrder(Comparator.comparing(m -> (long) m.get("qtity"))));
		}
        catch (Exception e) {
        	System.out.println(e.getMessage());
        }
		
		return _topsN;
		
	}


    private List<BreederAffixVariety> extractTopNVariety(int _year, List<BreederStatistics> _topsNAffixes) {

		List<BreederAffixVariety> _varietyList = new ArrayList<BreederAffixVariety>(); 
		int _id = 0;
		String _name = "";

		Map<TupleVariety,List<BreederStatistics>> _allVariety = _topsNAffixes
				.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())))
		;
		
		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if ( _allVariety.size() == 1 
				&& StreamUtils.breedMonoVariety(_allVariety.keySet().stream().findFirst().get().getName()) ) 
			return _varietyList;
		
		for (Map.Entry<TupleVariety,List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {
			
			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			BreederAffixVariety _variety = new BreederAffixVariety()
					.withId(_id)
					.withName(_name)
					.withAffixes(extractTopNOverYear(_year, _currentVariety.getValue()))
			;		
			_varietyList.add(_variety);

		}
		
		return _varietyList;
	}
    
    private boolean matchesRange(Entry<Integer, Long> e, int range_min, int range_max) {
        
    	if (range_min > 0 && range_max == 0)
    		return ( e.getValue() == range_min);
    	if (range_min > 0 && range_max > 0)
    		return ( e.getValue() >= range_min && e.getValue() <= range_max );
    	if (range_min == 0 && range_max > 0)
    		return ( e.getValue() > range_max);
    	return false;
   } 
    
    private int sumLitter(List<BreederStatistics> _list) {    	
    	
		Map<Integer, Long> _breeder = _list
				.stream()
				.collect(
		                Collectors.groupingBy(
		                		BreederStatistics::getIdEleveur, Collectors.counting()
		                )
		        );
		;
		
    	return _breeder.size();
    }

    private List<BreederStatistics> extractTopNAffixes (List<BreederStatistics> _list) {
    	
    	List<BreederStatistics> _topNAffixes = new ArrayList<BreederStatistics>();
    	
		// 1. On groupe les affixes par qtites (ne prends pas en compte les affixes vides)
		Map<String, Long> _affixes = _list
				.stream()
				.filter(x -> (!"".equals(x.getAffixeEleveur()) && x.getAffixeEleveur()!= null) )
				.collect(
		                Collectors.groupingBy(
		                		BreederStatistics::getAffixeEleveur, Collectors.counting()
		                )
		        );
		;
		
		// 2. On ne conserve que les 20 plus meilleurs
		Set<String> _sortedAffixes = _affixes.entrySet().stream()
			    .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
			    .limit(limitTopN)
			    .map(Entry::getKey)
			    .collect(Collectors.toSet())
		;
		
		// On conserve le topN affixe (null-safe way)
		this.allTopN = Optional.ofNullable(_sortedAffixes)
		  .map(Set::stream)
		  .orElseGet(Stream::empty)
		  .collect(Collectors.toSet())
		 ;
		
		
		// 3. On (re)construit la liste qui sera utilisée pour la lecture des filtres par année et/ou par mois. 
		_topNAffixes = _list
				.stream()
				.filter(x -> _sortedAffixes.contains(x.getAffixeEleveur()))
				.collect(Collectors.toList())
		;
		
		return _topNAffixes;
    }
    
}
