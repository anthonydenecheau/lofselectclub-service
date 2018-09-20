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
import com.scc.lofselectclub.model.ConfigurationRace;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.breeder.BreederAffixStatistics;
import com.scc.lofselectclub.template.parent.ParentVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.parent.ParentFather;
import com.scc.lofselectclub.template.parent.ParentBreed;
import com.scc.lofselectclub.template.parent.ParentResponseObject;
import com.scc.lofselectclub.template.parent.ParentBreedStatistics;
import com.scc.lofselectclub.template.parent.ParentFatherStatistics;
import com.scc.lofselectclub.template.parent.ParentGender;
import com.scc.lofselectclub.template.parent.ParentRegisterType;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ParentService {

    private static final Logger logger = LoggerFactory.getLogger(ParentService.class);
    
    @Autowired
    private Tracer tracer;

    @Autowired
    private BreederRepository breederRepository;
    
    @Autowired
    private ConfigurationRaceRepository configurationRaceRepository;
    
    @Autowired
    ServiceConfig config;

    int limitTopN = 0;
    
	@HystrixCommand(fallbackMethod = "buildFallbackParentList",
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
    public ParentResponseObject getStatistics(int idClub) throws EntityNotFoundException {

        Span newSpan = tracer.createSpan("getStatistics");
        logger.debug("In the parentService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

        // topN etalon
        limitTopN = config.getLimitTopNFathers();
        
        int _id = 0;
        String _name = "";

        List<ParentBreed> _breeds = new ArrayList<ParentBreed>();

        try {
        	
            // Lecture des races associées au club
            Map<TupleBreed,List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub)
    			.stream()
    			.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())))
    		;
            
            // Exception si le club n'a pas de races connues
            if (_allBreeds.size() == 0)
            	throw new EntityNotFoundException(ParentResponseObject.class, "idClub", String.valueOf(idClub));
            
            for (Map.Entry<TupleBreed,List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {
            
            	List<ParentFatherStatistics> _fathersStatistics = new ArrayList<ParentFatherStatistics>();  
            	
            	 int _year = 0;
            	_id = _currentBreed.getKey().getId();
            	_name = _currentBreed.getKey().getName();

            	List<ParentBreedStatistics> _breedStatistics = new ArrayList<ParentBreedStatistics>();  		

            	// Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
            	// A voir si les années précédentes ne feront pas l'objet d'une suppression côté data (BdD); auquel cas, ce code sera obsolète 
            	ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(_id);
            	int [] _serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
            	final int minYear =_serieYear[0];
            	
            	// Lecture des années (on ajoute un tri) 
            	Map<Integer,List<BreederStatistics>> _breedGroupByYear= _currentBreed.getValue()
            			.stream()
            			.filter(x -> x.getAnnee() >= minYear)
            			.collect(StreamUtils.sortedGroupingBy(BreederStatistics::getAnnee))
            	;
            	for (Map.Entry<Integer,List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

                	_year = _breedOverYear.getKey();
                	
                	// Suppression de l'année traitée
                	_serieYear=ArrayUtils.removeElement(_serieYear,_year); 
                	
                	List<Map<String, List<ParentGender>>> _origins = new ArrayList<Map<String, List<ParentGender>>>();
                	Map<String, List<ParentGender>> _origin = new HashMap<String, List<ParentGender>>();
                	
                	List<ParentGender> _statsFather = extractFather(_breedOverYear.getValue());
                	_origin.put("father", _statsFather);

                	List<ParentGender> _statsMother = extractMother(_breedOverYear.getValue());
                	_origin.put("mother", _statsMother);
                	
                	_origins.add(_origin);
                	
            		// Lecture des variétés s/ la race en cours (et pour l'année en cours)
                	List<ParentVariety> _variety = extractVariety(_breedOverYear.getValue());

                	ParentBreedStatistics _breed = new ParentBreedStatistics()
            			.withYear(_year)
            			.withOrigins(_origins)
            			.withVariety(_variety)
            		;
                	_breedStatistics.add(_breed);

                	// Recherche TopN Etalon de l'année en cours s/ la race et sur les varietes
                	List<ParentFather> _topsN = extractTopNFathers(limitTopN, _breedOverYear.getValue());
                	ParentFatherStatistics _fatherTopN = new ParentFatherStatistics()
                			.withYear(_year)
                			.withFathers(_topsN)
                			//.withVariety(_topNVariety)
                	;
                	_fathersStatistics.add(_fatherTopN);
                	
            	}

            	// On finalise en initialisant les années pour lesquelles on a constaté une rupture
            	for (int i = 0; i < _serieYear.length; i++) {
            		ParentBreedStatistics _breed = new ParentBreedStatistics()
            			.withYear(_serieYear[i])
            			.withOrigins(new ArrayList<Map<String, List<ParentGender>>>())
            			.withVariety(new ArrayList<ParentVariety>())
            		;	
            		_breedStatistics.add(_breed);
            		
            		ParentFatherStatistics _fatherTopN = new ParentFatherStatistics()
                			.withYear(_serieYear[i])
                			.withFathers(new ArrayList<ParentFather>())
                			//.withVariety(_topNVariety)
                	;
                	_fathersStatistics.add(_fatherTopN);
            	}
            	
            	// Création de l'objet Race
            	ParentBreed _breed = new ParentBreed()
            		.withId(_id)
            		.withName(_name)
            		.withStatistics(_breedStatistics)
            		.withTopN(_fathersStatistics)
            	;
            	
            	// Ajout à la liste 
            	_breeds.add(_breed);
            
            }
        	
            // Réponse
        	return new ParentResponseObject()
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

	private ParentResponseObject buildFallbackParentList(int idClub){
    	
    	List<ParentBreed> list = new ArrayList<ParentBreed>(); 
    	list.add(new ParentBreed()
                .withId(0))
    	;
        return new ParentResponseObject(list.size(),list);
    }
    
    private List<ParentGender> extractFather(List<BreederStatistics> _list) {

    	List<ParentGender> _stats = new ArrayList<ParentGender>();

    	int _qtity = 0;
    	int _qtityTypeFrancais = 0;
    	int _qtityTypeImport = 0;
    	int _qtityTypeEtranger = 0;
    	int _qtityTypeAutre = 0;

    	try { 
    		
        	// Male : Groupement par type d'inscription (sans doublons)
        	Map<Integer, Integer> _map = _list
        			.stream()
        			.collect(
        				Collectors.groupingBy(
        	        		BreederStatistics::getTypeEtalon,
        	        		Collectors.collectingAndThen(
        	                		Collectors.mapping(BreederStatistics::getIdEtalon, Collectors.toSet()),
        	                        Set::size)
        	               )
        	);
        	
	    	for (Integer key : _map.keySet()) {
				Integer value = _map.get(key);
	    	    
	    		switch (key) {
	    			case 537:  
	    				_qtityTypeFrancais += value;
	    				break;
	    			case 538:  
	    				_qtityTypeImport += value;
	    				break;
	    			case 540:  
	    				_qtityTypeEtranger += value;
	    				break;
	    			default:
	    				_qtityTypeAutre += value;
	    				break;
	    		}
	    		
	    	    // total tous types confondus
	    	    _qtity += value;
	    	}
	
	    	String[] _typesDefinition = {"FRANCAIS","IMPORTES","ETRANGERS","AUTRES"};
	    	List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
	    	NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
	    	
	    	for (String s: _typesDefinition) {
	    		int _qtityType = 0;
	    		double _percent = 0;
	    		switch (s) {
	    			case "FRANCAIS":
	    				_qtityType = _qtityTypeFrancais;
	    				break;
	    			case "IMPORTES":
	    				_qtityType = _qtityTypeImport;
	    				break;
	    			case "ETRANGERS":
	    				_qtityType = _qtityTypeEtranger;
	    				break;
	    			default:
	    				_qtityType = _qtityTypeAutre;
	    				break;
	    		}
	    		
	    		_percent = Precision.round((double)_qtityType/(double)_qtity,2);
	    		ParentRegisterType _type = new ParentRegisterType()
	    				.withRegistration(s)
	    				.withQtity(_qtityType)
	    				.withPercentage(format.format(_percent))
	    		;
	    		_types.add(_type);
	    	}
	
	    	ParentGender _male = new ParentGender()
	        		.withQtity(_qtity)
	        		.withRegisterType(_types)
	        ;
	    	_stats.add(_male);
	    	
    	} finally {
    		
    	}
    	
    	return _stats;
    }
    
    private List<ParentGender> extractMother(List<BreederStatistics> _list) {

    	List<ParentGender> _stats = new ArrayList<ParentGender>();

    	int _qtity = 0;
    	int _qtityTypeFrancais = 0;
    	int _qtityTypeImport = 0;
    	int _qtityTypeAutre = 0;

    	try {
    		
        	// Femelle : Groupement par type d'inscription (sans doublons)
        	Map<Integer, Integer> _map = _list
        			.stream()
        			.collect(
        				Collectors.groupingBy(
        	        		BreederStatistics::getTypeLice,
        	        		Collectors.collectingAndThen(
        	                		Collectors.mapping(BreederStatistics::getIdLice, Collectors.toSet()),
        	                        Set::size)
        	               )
        	);
        	
	    	for (Integer key : _map.keySet()) {
				Integer value = _map.get(key);
	    	    
	    		switch (key) {
	    			case 537:  
	    				_qtityTypeFrancais += value;
	    				break;
	    			case 538:  
	    				_qtityTypeImport += value;
	    				break;
	    			default:
	    				_qtityTypeAutre += value;
	    				break;
	    		}
	    		
	    	    // total tous types confondus
	    	    _qtity += value;
	    	}
	
	    	String[] _typesDefinition = {"FRANCAIS","IMPORTES","AUTRES"};
	    	List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
	    	NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);
	    	
	    	for (String s: _typesDefinition) {
	    		int _qtityType = 0;
	    		double _percent = 0;
	    		switch (s) {
	    			case "FRANCAIS":
	    				_qtityType = _qtityTypeFrancais;
	    				break;
	    			case "IMPORTES":
	    				_qtityType = _qtityTypeImport;
	    				break;
	    			default:
	    				_qtityType = _qtityTypeAutre;
	    				break;
	    		}
	    		
	    		_percent = Precision.round((double)_qtityType/(double)_qtity,2);
	    		ParentRegisterType _type = new ParentRegisterType()
	    				.withRegistration(s)
	    				.withQtity(_qtityType)
	    				.withPercentage(format.format(_percent))
	    		;
	    		_types.add(_type);
	    	}
	
	    	ParentGender _female = new ParentGender()
	        		.withQtity(_qtity)
	        		.withRegisterType(_types)
	        ;
	    	_stats.add(_female);
	    	
    	} finally {
    		
    	}
    	
    	return _stats;
    }
    
	private List<ParentVariety> extractVariety(List<BreederStatistics> _list) {

		List<ParentVariety> _varietyList = new ArrayList<ParentVariety>(); 
		int _id = 0;
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

        	List<Map<String, List<ParentGender>>> _origins = new ArrayList<Map<String, List<ParentGender>>>();
        	Map<String, List<ParentGender>> _origin = new HashMap<String, List<ParentGender>>();
        	
        	List<ParentGender> _statsFather = extractFather(_currentVariety.getValue());
        	_origin.put("father", _statsFather);

        	List<ParentGender> _statsMother = extractMother(_currentVariety.getValue());
        	_origin.put("mother", _statsMother);
        	
        	_origins.add(_origin);
        	
			// Création de l'objet Variety
			ParentVariety _variety = new ParentVariety()
					.withId(_id)
					.withName(_name)
					.withOrigins(_origins)
			;		
			_varietyList.add(_variety);
		}
		return _varietyList;
	}

	// Voir Stackoverflow : https://stackoverflow.com/questions/52343325/java-8-stream-how-to-get-top-n-count?noredirect=1#comment91673408_52343325
    private List<ParentFather> extractTopNFathers (int _limitTopN, List<BreederStatistics> _list) {
    	
    	List<ParentFather> _topNFathers = new ArrayList<ParentFather>();
    	
    	Map<BreederStatistics, Long> counts = _list
    			.stream()
    		    .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
		
    	Collection<BreederStatistics> _topN = StreamUtils.topN(counts, _limitTopN, BreederStatistics::getIdEtalon);
    	
    	Map<String, Long> _resume =  _topN
    			.stream()
    			.collect(
		                Collectors.groupingBy(
		                		BreederStatistics::getNomEtalon, Collectors.counting()
		                )
		);
    	
    	for (Map.Entry<String, Long> entry : _resume.entrySet()) {
    		ParentFather _currentEtalon = new ParentFather()
    			.withName(entry.getKey())
    			.withQtity((int) (long)entry.getValue())
    		;
    		_topNFathers.add(_currentEtalon);
    	}
    	
    	
		return _topNFathers;
    }
    
}
