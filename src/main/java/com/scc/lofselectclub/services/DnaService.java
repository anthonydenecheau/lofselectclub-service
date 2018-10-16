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
import com.scc.lofselectclub.model.ConfigurationClub;
import com.scc.lofselectclub.model.ConfigurationRace;
import com.scc.lofselectclub.model.DnaStatistics;
import com.scc.lofselectclub.repository.ConfigurationClubRepository;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.repository.DnaRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.dna.DnaBreedStatistics;
import com.scc.lofselectclub.template.dna.DnaVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.dna.DnaBreed;
import com.scc.lofselectclub.template.dna.DnaResponseObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	private ConfigurationClubRepository configurationClubRepository;

	@Autowired
	ServiceConfig config;

	int idBreed = 0;
	int idVariety = 0;
	
	/**
	 * Retourne les données statistiques liées à l'ADN pour l'ensemble des races affiliées au club
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>DnaResponseObject</code>
	 * @throws EntityNotFoundException
	 */
	@HystrixCommand(fallbackMethod = "buildFallbackDnaList", threadPoolKey = "getStatistics", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "30"),
			@HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
					@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
					@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
					@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
							EntityNotFoundException.class })
	public DnaResponseObject getStatistics(int idClub) throws EntityNotFoundException {

		Span newSpan = tracer.createSpan("getStatistics");
		logger.debug("In the DnaService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

		String _name = "";

		List<ConfigurationClub> _breedsManagedByClub = new ArrayList<ConfigurationClub>(); 
		List<DnaBreed> _breeds = new ArrayList<DnaBreed>();
		Map<TupleBreed, Set<TupleVariety>> _varietyByBreed = new HashMap<TupleBreed, Set<TupleVariety>>();

		try {

			// Initialisation des données races / varietes associées au club
			_breedsManagedByClub = configurationClubRepository.findByIdClub(idClub);

			// Exception si le club n'a pas de races connues == l'id club n'existe pas
			if (_breedsManagedByClub.size() == 0)
				throw new EntityNotFoundException(DnaResponseObject.class, "idClub", String.valueOf(idClub));

			// Intialisation des races du club			
			_varietyByBreed = _breedsManagedByClub.stream()
					 .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getLibelleRace()), 
                             Collectors.mapping( e -> new TupleVariety(e.getIdVariete(), e.getLibelleVariete()), Collectors.toSet())
                            )
					);
			
			// Lecture des races associées au club pour lesquelles des données ont été calculées
			Map<TupleBreed, List<DnaStatistics>> _allBreeds = dnaRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
			for (Map.Entry<TupleBreed, List<DnaStatistics>> _currentBreed : _allBreeds.entrySet()) {

				int _year = 0;
				this.idBreed = _currentBreed.getKey().getId();
				_name = _currentBreed.getKey().getName();

				List<DnaBreedStatistics> _breedStatistics = new ArrayList<DnaBreedStatistics>();

				// Lecture de la dernière date de calcul pour définir la période (rupture dans
				// les années)
				// A voir si les années précédentes ne feront pas l'objet d'une suppression côté
				// data (BdD); auquel cas, ce code sera obsolète
				ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(this.idBreed);
				int[] _serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
				final int minYear = _serieYear[0];

				// Lecture des variétés référencées 
				// si une variété n'est pas représentée pour l'année, il faut l'ajouter avec qtity = 0
				List<TupleVariety> _referencedVarieties = _varietyByBreed.entrySet()
						.stream()
						.filter(r -> r.getKey().getId() == this.idBreed)
						.flatMap(map -> map.getValue().stream())
						.collect(Collectors.toList())
				;
				
				// Lecture des années (on ajoute un tri)
				Map<Integer, List<DnaStatistics>> _breedGroupByYear = _currentBreed.getValue().stream()
						.filter(x -> x.getAnnee() >= minYear)
						.collect(StreamUtils.sortedGroupingBy(DnaStatistics::getAnnee));
				for (Map.Entry<Integer, List<DnaStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

					_year = _breedOverYear.getKey();

					// Suppression de l'année traitée
					_serieYear = ArrayUtils.removeElement(_serieYear, _year);

					// Somme des resultats Adn
					DnaStatistics sumDna = _breedOverYear.getValue().stream().reduce(new DnaStatistics(0, 0, 0, 0),
							(x, y) -> {
								return new DnaStatistics(x.getDna() + y.getDna(), x.getDnaComp() + y.getDnaComp(),
										x.getDnaCompP() + y.getDnaCompP(), x.getDnaCompM() + y.getDnaCompM());
							});

					// Lecture des variétés s/ la race en cours (et pour l'année en cours)
					List<DnaVariety> _variety = extractVariety(_breedOverYear.getValue(), _referencedVarieties);

					DnaBreedStatistics _breed = new DnaBreedStatistics().withYear(_year).withDna(sumDna.getDna())
							.withDnaComp(sumDna.getDnaComp()).withDnaCompP(sumDna.getDnaCompP())
							.withDnaCompM(sumDna.getDnaCompM()).withVariety(_variety);
					_breedStatistics.add(_breed);

				}

				// On finalise en initialisant les années pour lesquelles on a constaté une rupture
				for (int i = 0; i < _serieYear.length; i++) {
					DnaBreedStatistics _breed = new DnaBreedStatistics().withYear(_serieYear[i]).withDna(0)
							.withDnaComp(0).withDnaCompP(0).withDnaCompM(0).withVariety(extractVariety(new ArrayList<DnaStatistics>(), _referencedVarieties));
					_breedStatistics.add(_breed);
				}

				// Création de l'objet Race
				DnaBreed _breed = new DnaBreed().withId(this.idBreed).withName(_name).withStatistics(_breedStatistics);

				// Ajout à la liste
				_breeds.add(_breed);

			}

			// Réponse
			return new DnaResponseObject().withBreeds(_breeds).withSize(_breeds.size());

		} finally {
			newSpan.tag("peer.service", "postgres");
			newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
			tracer.close(newSpan);
		}
	}

	/**
	 * Fonction fallbackMethod de la fonction principale <code>getStatistics</code> (Hystrix Latency / Fault Tolerance)
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>DnaResponseObject</code>
	 */
	private DnaResponseObject buildFallbackDnaList(int idClub) {

		List<DnaBreed> list = new ArrayList<DnaBreed>();
		list.add(new DnaBreed().withId(0));
		return new DnaResponseObject(list.size(), list);
	}

	/**
	 * Retourne les données statistiques pour l'ensemble des variétés de la race
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>variety</code> de l'objet <code>DnaBreedStatistics</code>
	 */
	private List<DnaVariety> extractVariety(List<DnaStatistics> _list, List<TupleVariety> _referencedVarieties) {

		List<DnaVariety> _varietyList = new ArrayList<DnaVariety>();
		this.idVariety = 0;
		String _name = "";

		Map<TupleVariety, List<DnaStatistics>> _allVariety = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));

		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if (_referencedVarieties.size() == 1
				&& StreamUtils.breedMonoVariety(_referencedVarieties.stream().findFirst().orElse(new TupleVariety(0,""))) )
			return _varietyList;

		// On stocke la liste des variétés pour la race 
		List<TupleVariety> _varieties  = new ArrayList<TupleVariety>(_referencedVarieties);
		
		for (Map.Entry<TupleVariety, List<DnaStatistics>> _currentVariety : _allVariety.entrySet()) {

			this.idVariety = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			// Somme des resultats Adn
			DnaStatistics sumDna = _currentVariety.getValue().stream().reduce(new DnaStatistics(0, 0, 0, 0),
					(x, y) -> {
						return new DnaStatistics(x.getDna() + y.getDna(), x.getDnaComp() + y.getDnaComp(),
								x.getDnaCompP() + y.getDnaCompP(), x.getDnaCompM() + y.getDnaCompM());
					});

			// Création de l'objet Variety
			DnaVariety _variety = new DnaVariety().withId(this.idVariety).withName(_name).withDna(sumDna.getDna())
					.withDnaComp(sumDna.getDnaComp()).withDnaCompP(sumDna.getDnaCompP())
					.withDnaCompM(sumDna.getDnaCompM());
			_varietyList.add(_variety);

			// Suppression de la variété traitée
			_varieties.removeIf(e -> e.getId() == this.idVariety);

		}

		// Toutes les variétés n'ont pas fait l'objet d'une production doivent être mentionnées
		if (_varieties.size()>0) {
			for (TupleVariety v : _varieties) {
				DnaVariety _variety = new DnaVariety().withId(v.getId()).withName(v.getName())
						.withDna(0).withDnaComp(0).withDnaCompP(0).withDnaCompM(0);
				_varietyList.add(_variety);
			}
		}
		return _varietyList;

	}

}
