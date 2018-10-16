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
import com.scc.lofselectclub.model.ConfigurationClub;
import com.scc.lofselectclub.model.ConfigurationRace;
import com.scc.lofselectclub.repository.BreederRepository;
import com.scc.lofselectclub.repository.ConfigurationClubRepository;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityBreed;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityBreedStatistics;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityResponseObject;
import com.scc.lofselectclub.template.consanguinity.ConsanguinityVariety;
import com.scc.lofselectclub.template.consanguinity.ConsanguintyCommonAncestor;
import com.scc.lofselectclub.utils.StreamUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class ConsanguinityService {

	private static final Logger logger = LoggerFactory.getLogger(ConsanguinityService.class);

	@Autowired
	private Tracer tracer;

	@Autowired
	private BreederRepository breederRepository;

	@Autowired
	private ConfigurationRaceRepository configurationRaceRepository;

	@Autowired
	private ConfigurationClubRepository configurationClubRepository;

	@Autowired
	ServiceConfig config;

	int idBreed = 0;
	int idVariety = 0;
	
	/**
	 * Retourne les données statistiques liées à la consanguinité pour l'ensemble des races affiliées au club
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>ConsanguinityResponseObject</code>
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

		String _name = "";

		List<ConfigurationClub> _breedsManagedByClub = new ArrayList<ConfigurationClub>(); 
		List<ConsanguinityBreed> _breeds = new ArrayList<ConsanguinityBreed>();
		Map<TupleBreed, Set<TupleVariety>> _varietyByBreed = new HashMap<TupleBreed, Set<TupleVariety>>();

		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		try {

			// Initialisation des données races / varietes associées au club
			_breedsManagedByClub = configurationClubRepository.findByIdClub(idClub);

			// Exception si le club n'a pas de races connues == l'id club n'existe pas
			if (_breedsManagedByClub.size() == 0)
				throw new EntityNotFoundException(ConsanguinityResponseObject.class, "idClub", String.valueOf(idClub));

			// Intialisation des races du club			
			_varietyByBreed = _breedsManagedByClub.stream()
					 .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getLibelleRace()), 
                             Collectors.mapping( e -> new TupleVariety(e.getIdVariete(), e.getLibelleVariete()), Collectors.toSet())
                            )
					);

			// Lecture des races associées au club pour lesquelles des données ont été calculées
			Map<TupleBreed, List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
			for (Map.Entry<TupleBreed, List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {

				int _year = 0;
				this.idBreed = _currentBreed.getKey().getId();
				_name = _currentBreed.getKey().getName();

				List<ConsanguinityBreedStatistics> _breedStatistics = new ArrayList<ConsanguinityBreedStatistics>();

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
				Map<Integer, List<BreederStatistics>> _breedGroupByYear = _currentBreed.getValue().stream()
						.filter(x -> x.getAnnee() >= minYear)
						.collect(StreamUtils.sortedGroupingBy(BreederStatistics::getAnnee));
				for (Map.Entry<Integer, List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

					_year = _breedOverYear.getKey();

					// Suppression de l'année traitée
					_serieYear = ArrayUtils.removeElement(_serieYear, _year);

					// Moyenne des coef. de consanguinité
					double _cng = _breedOverYear.getValue().stream().mapToDouble(BreederStatistics::getConsanguinite)
							.average().orElse(0.0);

					// Nb portées par nb d'ancètres communs
					List<ConsanguintyCommonAncestor> _commonAncestors = extractCommonAncestors(
							_breedOverYear.getValue());

					// Lecture des variétés s/ la race en cours (et pour l'année en cours)
					List<ConsanguinityVariety> _variety = extractVariety(_breedOverYear.getValue(), _referencedVarieties);

					ConsanguinityBreedStatistics _breed = new ConsanguinityBreedStatistics().withYear(_year)
							.withCng(format.format(Precision.round(_cng, 2)))
							.withLitterByCommonAncestor(_commonAncestors).withVariety(_variety);
					_breedStatistics.add(_breed);

				}

				// On finalise en initialisant les années pour lesquelles on a constaté une rupture
				for (int i = 0; i < _serieYear.length; i++) {
					ConsanguinityBreedStatistics _breed = new ConsanguinityBreedStatistics().withYear(_serieYear[i])
							.withCng(format.format((double) 0))
							.withLitterByCommonAncestor(new ArrayList<ConsanguintyCommonAncestor>())
							.withVariety(extractVariety(new ArrayList<BreederStatistics>(), _referencedVarieties));
					_breedStatistics.add(_breed);
				}

				// Création de l'objet Race
				ConsanguinityBreed _breed = new ConsanguinityBreed().withId(this.idBreed).withName(_name)
						.withStatistics(_breedStatistics);

				// Ajout à la liste
				_breeds.add(_breed);

			}

			// Réponse
			return new ConsanguinityResponseObject().withBreeds(_breeds).withSize(_breeds.size());

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
	 * @return			Objet <code>ConsanguinityResponseObject</code>
	 */
	private ConsanguinityResponseObject buildFallbackConsanguinityList(int idClub) {

		List<ConsanguinityBreed> list = new ArrayList<ConsanguinityBreed>();
		list.add(new ConsanguinityBreed().withId(0));
		return new ConsanguinityResponseObject(list.size(), list);
	}

	/**
	 * Retourne les données statistiques pour l'ensemble des variétés de la race
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>variety</code> de l'objet <code>ConsanguinityBreedStatistics</code>
	 */
	private List<ConsanguinityVariety> extractVariety(List<BreederStatistics> _list, List<TupleVariety> _referencedVarieties) {

		List<ConsanguinityVariety> _varietyList = new ArrayList<ConsanguinityVariety>();
		this.idVariety = 0;
		String _name = "";

		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		Map<TupleVariety, List<BreederStatistics>> _allVariety = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));

		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if (_referencedVarieties.size() == 1
				&& StreamUtils.breedMonoVariety(_referencedVarieties.stream().findFirst().orElse(new TupleVariety(0,""))) )
			return _varietyList;

		// On stocke la liste des variétés pour la race 
		List<TupleVariety> _varieties  = new ArrayList<TupleVariety>(_referencedVarieties);
		
		for (Map.Entry<TupleVariety, List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {

			this.idVariety = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			// Moyenne des coef. de consanguinité
			double _cng = _currentVariety.getValue().stream().mapToDouble(BreederStatistics::getConsanguinite).average()
					.orElse(0.0);

			// Moyenne du nb d'ancètres communs
			List<ConsanguintyCommonAncestor> _commonAncestors = extractCommonAncestors(_currentVariety.getValue());

			// Création de l'objet Variety
			ConsanguinityVariety _variety = new ConsanguinityVariety().withId(this.idVariety).withName(_name)
					.withCng(format.format(Precision.round(_cng, 2))).withLitterByCommonAncestor(_commonAncestors);
			_varietyList.add(_variety);

			// Suppression de la variété traitée
			_varieties.removeIf(e -> e.getId() == this.idVariety);
		}

		// Toutes les variétés n'ont pas fait l'objet d'une production doivent être mentionnées
		if (_varieties.size()>0) {
			for (TupleVariety v : _varieties) {
				ConsanguinityVariety _variety = new ConsanguinityVariety().withId(v.getId()).withName(v.getName())
						.withCng(format.format((double) 0)).withLitterByCommonAncestor(new ArrayList<ConsanguintyCommonAncestor>());
				_varietyList.add(_variety);
			}
		}
		return _varietyList;

	}

	/**
	 * Retourne le nombre de portées ayant n ancêtres communs
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @return						Propriété <code>litterByCommonAncestor</code> de l'objet <code>ConsanguinityBreedStatistics</code>
	 */
	private List<ConsanguintyCommonAncestor> extractCommonAncestors(List<BreederStatistics> _list) {

		List<ConsanguintyCommonAncestor> _commonAncestors = new ArrayList<ConsanguintyCommonAncestor>();

		Map<Integer, Integer> _breedAndCommonAncestor = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getNbAncetreCommun, Collectors.collectingAndThen(
						Collectors.mapping(BreederStatistics::getIdSaillie, Collectors.toSet()), Set::size)));

		// Remarque : la liste _breedAndCommonAncestor contient nombre d'ancêtres communs, la liste des dossier
		// il faut maintenant compter de min à max (nb d'ancêtres commun), le nombre de dossiers
		SortedMap<Integer, Integer> _series = new TreeMap<Integer, Integer>(_breedAndCommonAncestor);

		Integer highestKey = _series.lastKey();
		// Integer lowestKey = _series.firstKey();

		if (_series.size() > 0)
			for (int i = 1; i <= highestKey; i++) {
				ConsanguintyCommonAncestor c = null;
				if (_series.containsKey(i))
					c = new ConsanguintyCommonAncestor().withNumberOfCommonAncestor(i)
							.withNumberOfLitter(_series.get(i));
				else
					c = new ConsanguintyCommonAncestor().withNumberOfCommonAncestor(i).withNumberOfLitter(0);

				_commonAncestors.add(c);
			}

		return _commonAncestors;
	}
}
