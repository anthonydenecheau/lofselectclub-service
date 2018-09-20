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
import com.scc.lofselectclub.model.DnaStatistics;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.repository.DnaRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.dna.DnaBreedStatistics;
import com.scc.lofselectclub.template.dna.DnaVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.dna.DnaBreed;
import com.scc.lofselectclub.template.dna.DnaResponseObject;

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
public class DnaService {

    private static final Logger logger = LoggerFactory.getLogger(DnaService.class);
    
    @Autowired
    private Tracer tracer;

    @Autowired
    private DnaRepository dnaRepository;
    
    @Autowired
    private ConfigurationRaceRepository configurationRaceRepository;

    @Autowired
    ServiceConfig config;

	@HystrixCommand(fallbackMethod = "buildFallbackDnaList",
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
    public DnaResponseObject getStatistics(int idClub) throws EntityNotFoundException {

        Span newSpan = tracer.createSpan("getStatistics");
        logger.debug("In the DnaService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

        int _id = 0;
        String _name = "";

        List<DnaBreed> _breeds = new ArrayList<DnaBreed>();

        try {
        	
            // Lecture des races associées au club
            Map<TupleBreed,List<DnaStatistics>> _allBreeds = dnaRepository.findByIdClub(idClub)
    			.stream()
    			.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())))
    		;
            
            // Exception si le club n'a pas de races connues
            if (_allBreeds.size() == 0)
            	throw new EntityNotFoundException(DnaResponseObject.class, "idClub", String.valueOf(idClub));
            
            for (Map.Entry<TupleBreed,List<DnaStatistics>> _currentBreed : _allBreeds.entrySet()) {
            
            	int _year = 0;
            	_id = _currentBreed.getKey().getId();
            	_name = _currentBreed.getKey().getName();
            	
            	List<DnaBreedStatistics> _breedStatistics = new ArrayList<DnaBreedStatistics>();  		

            	// Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
            	// A voir si les années précédentes ne feront pas l'objet d'une suppression côté data (BdD); auquel cas, ce code sera obsolète 
            	ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(_id);
            	int [] _serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
            	final int minYear =_serieYear[0];
            		
            	// Lecture des années (on ajoute un tri) 
            	Map<Integer,List<DnaStatistics>> _breedGroupByYear= _currentBreed.getValue()
            			.stream()
            			.filter(x -> x.getAnnee() >= minYear)
            			.collect(StreamUtils.sortedGroupingBy(DnaStatistics::getAnnee))
            	;
            	for (Map.Entry<Integer,List<DnaStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

                	_year = _breedOverYear.getKey();
                	
                	// Suppression de l'année traitée
                	_serieYear=ArrayUtils.removeElement(_serieYear,_year); 
            		
                	// Somme des resultats Adn
                	DnaStatistics sumBirth = _breedOverYear.getValue()
                			.stream()
                		    .reduce(new DnaStatistics(0, 0, 0, 0), (x, y) -> {
                		         return new DnaStatistics(
                		        		 x.getDna() + y.getDna()
                		        		 , x.getDnaComp() + y.getDnaComp()
                		        		 , x.getDnaCompP() + y.getDnaCompP()
                		        		 , x.getDnaCompM() + y.getDnaCompM()
                		        		 );
                		    });
                	
            		// Lecture des variétés s/ la race en cours (et pour l'année en cours)
                	List<DnaVariety> _variety = extractVariety(_breedOverYear.getValue());

                	DnaBreedStatistics _breed = new DnaBreedStatistics()
            			.withYear(_year)
            			.withDna(sumBirth.getDna())
            			.withDnaComp(sumBirth.getDnaComp())
            			.withDnaCompP(sumBirth.getDnaCompP())
            			.withDnaCompM(sumBirth.getDnaCompM())
            			.withVariety(_variety)
            		;
                	_breedStatistics.add(_breed);

            	}
            	
            	// On finalise en initialisant les années pour lesquelles on a constaté une rupture
            	for (int i = 0; i < _serieYear.length; i++) {
            		DnaBreedStatistics _breed = new DnaBreedStatistics()
            			.withYear(_serieYear[i])
            			.withDna(0)
            			.withDnaComp(0)
            			.withDnaCompP(0)
            			.withDnaCompM(0)
            			.withVariety(new ArrayList<DnaVariety>())
            		;	
            		_breedStatistics.add(_breed);
            	}

            	// Création de l'objet Race
            	DnaBreed _breed = new DnaBreed()
            		.withId(_id)
            		.withName(_name)
            		.withStatistics(_breedStatistics)
            	;
            	
            	// Ajout à la liste 
            	_breeds.add(_breed);
            
            }
        	

            // Réponse
        	return new DnaResponseObject()
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

	private DnaResponseObject buildFallbackDnaList(int idClub){
    	
    	List<DnaBreed> list = new ArrayList<DnaBreed>(); 
    	list.add(new DnaBreed()
                .withId(0))
    	;
        return new DnaResponseObject(list.size(),list);
    }
    
	private List<DnaVariety> extractVariety(List<DnaStatistics> _list) {

		List<DnaVariety> _varietyList = new ArrayList<DnaVariety>(); 
		int _id = 0;
		String _name = "";
		
		Map<TupleVariety,List<DnaStatistics>> _allVariety = _list
				.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())))
		;
		
		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if ( _allVariety.size() == 1 
				&& StreamUtils.breedMonoVariety(_allVariety.keySet().stream().findFirst().get().getName()) ) 
			return _varietyList;
		
		for (Map.Entry<TupleVariety,List<DnaStatistics>> _currentVariety : _allVariety.entrySet()) {
			
			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

        	// Somme des chiots males, femelles, portée
        	DnaStatistics sumBirth = _currentVariety.getValue()
        			.stream()
        		    .reduce(new DnaStatistics(0, 0, 0, 0), (x, y) -> {
       		         return new DnaStatistics(
    		        		 x.getDna() + y.getDna()
    		        		 , x.getDnaComp() + y.getDnaComp()
    		        		 , x.getDnaCompP() + y.getDnaCompP()
    		        		 , x.getDnaCompM() + y.getDnaCompM());
    		        });
        	
			// Création de l'objet Variety
			DnaVariety _variety = new DnaVariety()
					.withId(_id)
					.withName(_name)
        			.withDna(sumBirth.getDna())
        			.withDnaComp(sumBirth.getDnaComp())
        			.withDnaCompP(sumBirth.getDnaCompP())
        			.withDnaCompM(sumBirth.getDnaCompM())
			;		
			_varietyList.add(_variety);

		}
		
		return _varietyList;
	
	}
	
}
