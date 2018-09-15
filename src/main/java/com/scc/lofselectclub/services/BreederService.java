package com.scc.lofselectclub.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.scc.lofselectclub.config.ServiceConfig;
import com.scc.lofselectclub.model.BreederStatistics;
import com.scc.lofselectclub.model.RangeDefinition;
import com.scc.lofselectclub.model.RangeRace;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.repository.RangeDefinitionRepository;
import com.scc.lofselectclub.repository.RangeRaceRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.breeder.MonthStatistics;
import com.scc.lofselectclub.template.breeder.AffixStatistics;
import com.scc.lofselectclub.template.breeder.AffixVariety;
import com.scc.lofselectclub.template.breeder.Breed;
import com.scc.lofselectclub.template.breeder.ResponseObject;
import com.scc.lofselectclub.template.breeder.BreedStatistics;
import com.scc.lofselectclub.template.breeder.VarietyStatistics;
import com.scc.lofselectclub.template.breeder.Variety;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
    private RangeRaceRepository rangeRaceRepository;

    @Autowired
    private RangeDefinitionRepository rangeDefinitionRepository;

    @Autowired
    ServiceConfig config;

    private Integer minVal = 0;
    private Integer maxVal = 0;
    
    private Set<String> allTopN = new HashSet<String>();
    final int limitTopN = 20;

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
                     @HystrixProperty(name="metrics.rollingStats.numBuckets", value="5")}
    )
    public ResponseObject getStatistics(int idClub){

        Span newSpan = tracer.createSpan("getStatistics");
        logger.debug("In the breederService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

    	long startTime = System.currentTimeMillis();
        int _id = 0;
        String _name = "";
        String period = "";

        List<Breed> _breeds = new ArrayList<Breed>();
        
        try {
        	
            // Lecture des races associées au club
            Map<TupleBreed,List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub)
    			.stream()
    			.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())))
    		;
            for (Map.Entry<TupleBreed,List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {

            	List<AffixStatistics> _affixesStatistics = new ArrayList<AffixStatistics>();  
            	List<BreedStatistics> _breedStatistics = new ArrayList<BreedStatistics>();  		
            	int _year = 0;
            	int _qtity = 0;
            	
            	_id = _currentBreed.getKey().getId();
            	_name = _currentBreed.getKey().getName();
            	System.out.println("Race :"+_id+"@"+_name);

            	// Recherche TopN Affixe s/ la race
            	List<BreederStatistics> _topsNAffixes = extractTopNAffixes(_currentBreed.getValue());

                // Lecture des plages paramétrées pour la race en cours
                RangeRace _rangeRace = rangeRaceRepository.findByIdRace(_id);
                List<RangeDefinition> plages = rangeDefinitionRepository.findByIdRangeGroupOrderBySequence(_rangeRace.getIdRangeGroup());
                period = _rangeRace.getBreakPeriod();

            	// TODO : Quid des années pour lesquels aucune production n'a été enregistrée

            	// Lecture des années (on ajoute un tri) 
            	Map<Integer,List<BreederStatistics>> _breedGroupByYear= _currentBreed.getValue()
            			.stream()
            			.collect(sortedGroupingBy(BreederStatistics::getAnnee))
            	;
            	for (Map.Entry<Integer,List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

                	List<MonthStatistics> _months = new ArrayList<MonthStatistics>();  		
                	_year = _breedOverYear.getKey();
                	
                	// Total de la production de l'année en cours
                	_qtity = sumLitter(_breedOverYear.getValue());
                	
                	// Recherche Production de l'année en cours pour les plages paramétrées s/ la race
                	List<Map<String, Object>> _series = extractSeries(plages, _breedOverYear.getValue());
            		
            		// Lecture des variétés s/ la race en cours (et pour l'année en cours)
                	List<Variety> _variety = extractVariety(plages, _breedOverYear.getValue());

                	// TODO : Quid des mois pour lesquels aucune production n'a été enregistrée
                	if ("MM".equals(period)) {
                		
                		int _month = 0;
                		int _monthQtity = 0;
                		int[] _listMonths = new int[] {1,2,3,4,5,6,7,8,9,10,11,12};
                		
                		// On complète les données mensuelles (on ajoute un tri == permet de détecter les ruptures dans la production)
                    	Map<Integer,List<BreederStatistics>> _breedGroupByMonth = _breedOverYear.getValue()
                    			.stream()
                    			.collect(sortedGroupingBy(BreederStatistics::getMois))
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
                        	List<Variety> _varietyMonth = extractVariety(plages, _breedOverMonth.getValue());

                        	MonthStatistics _monthStatitics = new MonthStatistics()
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
                    				new MonthStatistics()
                    					.withMonth(_listMonths[i])
                    					.withQtity(0)
                    					.withSeries(new ArrayList<Map<String, Object>>())
                    					.withVariety(new ArrayList<Variety>())                    			
                        		);
                    		}
                    	
                    	// Mise à jour == Tri s/ les mois
                    	_months.sort(Comparator.comparing(MonthStatistics::getMonth));
                	}
                	
                	// Création de l'objet Statistique de l'année en cours pour la race (inclus variété)
                	BreedStatistics _breed = new BreedStatistics()
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
                	List<AffixVariety> _topNVariety = extractTopNVariety(_year, _topsNAffixes);

                	AffixStatistics _affixStatistics = new AffixStatistics()
                			.withYear(_year)
                			.withAffixes(_topsN)
                			.withVariety(_topNVariety)
                	;
                	_affixesStatistics.add(_affixStatistics);

            	}
            	
            	// Création de l'objet Race
            	Breed _breed = new Breed()
            		.withId(_id)
            		.withName(_name)
            		.withStatistics(_breedStatistics)
            		.withTopN(_affixesStatistics)
            	;
            	
            	// Ajout à la liste 
            	_breeds.add(_breed);
            }

            long endTime = System.currentTimeMillis();
            long timeElapsed = endTime - startTime;

            System.out.println("Execution time in milliseconds: " + timeElapsed);
            
            // Réponse
        	return new ResponseObject()
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

	private ResponseObject buildFallbackBreederList(int idClub){
    	
    	List<Breed> list = new ArrayList<Breed>(); 
    	list.add(new Breed()
                .withId(0))
    	;
        return new ResponseObject(list.size(),list);
    }
    
	private List<Variety> extractVariety(List<RangeDefinition> plages, List<BreederStatistics> _list) {

		List<Variety> _varietyList = new ArrayList<Variety>(); 
		int _id = 0;
		int _qtity = 0;
		String _name = "";
		
		Map<TupleVariety,List<BreederStatistics>> _allVariety = _list
				.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())))
		;
		for (Map.Entry<TupleVariety,List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {
			
			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

        	// Total de la production de l'année en cours
			_qtity = sumLitter(_currentVariety.getValue());
    		
			// Recherche Production pour les plages paramétrées s/ la variété en cours
			List<Map<String, Object>> _series = extractSeries(plages, _currentVariety.getValue());

			// Création de l'objet VarietyStatistics
			VarietyStatistics _varietyStatistics = new VarietyStatistics()
					.withQtity(_qtity)
					.withSeries(_series)
			;

			// Création de l'objet Variety
			Variety _variety = new Variety()
					.withId(_id)
					.withName(_name)
					.withStatistics(_varietyStatistics)
			;		
			_varietyList.add(_variety);
		}
		return _varietyList;
	}

	private List<Map<String, Object>> extractSeries(List<RangeDefinition> plages, List<BreederStatistics> _list) {
		
    	List<Map<String, Object>> _series = new ArrayList<Map<String, Object>>();

		// Par tranche
		for (RangeDefinition plage : plages) {
			
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


    private List<AffixVariety> extractTopNVariety(int _year, List<BreederStatistics> _topsNAffixes) {

		List<AffixVariety> _varietyList = new ArrayList<AffixVariety>(); 
		int _id = 0;
		String _name = "";

		Map<TupleVariety,List<BreederStatistics>> _allVariety = _topsNAffixes
				.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())))
		;
		for (Map.Entry<TupleVariety,List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {
			
			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			AffixVariety _variety = new AffixVariety()
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

    private <T, K extends Comparable<K>> Collector<T, ?, TreeMap<K, List<T>>> sortedGroupingBy(Function<T, K> function) {
         return Collectors.groupingBy(function, 
            TreeMap::new, Collectors.toList());
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
