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
import com.scc.lofselectclub.model.ConfigurationRace;
import com.scc.lofselectclub.model.ConfirmationStatistics;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.repository.ConfirmationRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.confirmation.ConfirmationBreedStatistics;
import com.scc.lofselectclub.template.confirmation.ConfirmationVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.confirmation.ConfirmationBreed;
import com.scc.lofselectclub.template.confirmation.ConfirmationResponseObject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ConfirmationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfirmationService.class);
    
    @Autowired
    private Tracer tracer;

    @Autowired
    private ConfirmationRepository confirmationRepository;
    
    @Autowired
    private ConfigurationRaceRepository configurationRaceRepository;

    @Autowired
    ServiceConfig config;

	@HystrixCommand(fallbackMethod = "buildFallbackConfirmationList",
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
    public ConfirmationResponseObject getStatistics(int idClub) throws EntityNotFoundException {

        Span newSpan = tracer.createSpan("getStatistics");
        logger.debug("In the ConfirmationService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

        int _id = 0;
        String _name = "";

        List<ConfirmationBreed> _breeds = new ArrayList<ConfirmationBreed>();

        try {
        	
            // Lecture des races associées au club
            Map<TupleBreed,List<ConfirmationStatistics>> _allBreeds = confirmationRepository.findByIdClub(idClub)
    			.stream()
    			.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())))
    		;
            
            // Exception si le club n'a pas de races connues
            if (_allBreeds.size() == 0)
            	throw new EntityNotFoundException(ConfirmationResponseObject.class, "idClub", String.valueOf(idClub));
            
            for (Map.Entry<TupleBreed,List<ConfirmationStatistics>> _currentBreed : _allBreeds.entrySet()) {
            
            	int _year = 0;
            	Long _qtity = null;
            	
            	_id = _currentBreed.getKey().getId();
            	_name = _currentBreed.getKey().getName();

            	List<ConfirmationBreedStatistics> _breedStatistics = new ArrayList<ConfirmationBreedStatistics>();  		

            	// Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
            	// A voir si les années précédentes ne feront pas l'objet d'une suppression côté data (BdD); auquel cas, ce code sera obsolète 
            	ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(_id);
            	int [] _serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
            	final int minYear =_serieYear[0];
            	
            	// Lecture des années (on ajoute un tri) 
            	Map<Integer,List<ConfirmationStatistics>> _breedGroupByYear= _currentBreed.getValue()
            			.stream()
            			.filter(x -> x.getAnnee() >= minYear)
            			.collect(StreamUtils.sortedGroupingBy(ConfirmationStatistics::getAnnee))
            	;
            	for (Map.Entry<Integer,List<ConfirmationStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

                	_year = _breedOverYear.getKey();
                	
                	// Suppression de l'année traitée
                	_serieYear=ArrayUtils.removeElement(_serieYear,_year);
                	
                	// Nb de confirmations
                	_qtity = _breedOverYear.getValue()
        					.stream()
            				.collect(Collectors.counting()
            		);                	
        			
            		// Lecture des variétés s/ la race en cours (et pour l'année en cours)
                	List<ConfirmationVariety> _variety = extractVariety(_breedOverYear.getValue());

                	ConfirmationBreedStatistics _breed = new ConfirmationBreedStatistics()
            			.withYear(_year)
            			.withQtity( (int) (long) _qtity)
            			.withVariety(_variety)
            		;
                	_breedStatistics.add(_breed);

            	}

            	// On finalise en initialisant les années pour lesquelles on a constaté une rupture
            	for (int i = 0; i < _serieYear.length; i++) {
            		ConfirmationBreedStatistics _breed = new ConfirmationBreedStatistics()
            			.withYear(_serieYear[i])
            			.withQtity(0)
            			.withVariety(new ArrayList<ConfirmationVariety>())
            		;	
            		_breedStatistics.add(_breed);
            	}
            	
            	// Création de l'objet Race
            	ConfirmationBreed _breed = new ConfirmationBreed()
            		.withId(_id)
            		.withName(_name)
            		.withStatistics(_breedStatistics)
            	;
            	
            	// Ajout à la liste 
            	_breeds.add(_breed);
            
            }
        	
            // Réponse
        	return new ConfirmationResponseObject()
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

	private ConfirmationResponseObject buildFallbackConfirmationList(int idClub){
    	
    	List<ConfirmationBreed> list = new ArrayList<ConfirmationBreed>(); 
    	list.add(new ConfirmationBreed()
                .withId(0))
    	;
        return new ConfirmationResponseObject(list.size(),list);
    }
    
	private List<ConfirmationVariety> extractVariety(List<ConfirmationStatistics> _list) {

		List<ConfirmationVariety> _varietyList = new ArrayList<ConfirmationVariety>(); 
		int _id = 0;
		String _name = "";
		
		long _qtity = 0;
		
		Map<TupleVariety,List<ConfirmationStatistics>> _allVariety = _list
				.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())))
		;
		
		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if ( _allVariety.size() == 1 
				&& StreamUtils.breedMonoVariety(_allVariety.keySet().stream().findFirst().get().getName()) ) 
			return _varietyList;
		
		for (Map.Entry<TupleVariety,List<ConfirmationStatistics>> _currentVariety : _allVariety.entrySet()) {
			
			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

        	// Somme des chiots males, femelles, portée
        	_qtity = _currentVariety.getValue()
					.stream()
    				.collect(Collectors.counting()
    		);     
        	
			// Création de l'objet Variety
			ConfirmationVariety _variety = new ConfirmationVariety()
					.withId(_id)
					.withName(_name)
        			.withQtity((int) (long) _qtity)
			;		
			_varietyList.add(_variety);

		}
		
		return _varietyList;
	
	}

}
