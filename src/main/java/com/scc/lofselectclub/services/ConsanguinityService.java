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

		int _id = 0;
		String _name = "";

		List<ConsanguinityBreed> _breeds = new ArrayList<ConsanguinityBreed>();
		Map<Integer, Set<Integer>> _varietyByBreed = new HashMap<Integer, Set<Integer>>();

		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		try {

			// Initialisation des données races / varietes associées au club
			_varietyByBreed = configurationClubRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(ConfigurationClub::getIdRace,
							Collectors.mapping(ConfigurationClub::getIdVariete, Collectors.toSet())));

			// Exception si le club n'a pas de races connues == l'id club n'existe pas
			if (_varietyByBreed.size() == 0)
				throw new EntityNotFoundException(ConsanguinityResponseObject.class, "idClub", String.valueOf(idClub));

			// Lecture des races associées au club pour lesquelles des données ont été calculées
			Map<TupleBreed, List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
			for (Map.Entry<TupleBreed, List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {

				int _year = 0;
				_id = _currentBreed.getKey().getId();
				_name = _currentBreed.getKey().getName();

				List<ConsanguinityBreedStatistics> _breedStatistics = new ArrayList<ConsanguinityBreedStatistics>();

				// Lecture de la dernière date de calcul pour définir la période (rupture dans
				// les années)
				// A voir si les années précédentes ne feront pas l'objet d'une suppression côté
				// data (BdD); auquel cas, ce code sera obsolète
				ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(_id);
				int[] _serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
				final int minYear = _serieYear[0];

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
					List<ConsanguinityVariety> _variety = extractVariety(_breedOverYear.getValue());

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
							.withVariety(new ArrayList<ConsanguinityVariety>());
					_breedStatistics.add(_breed);
				}

				// Création de l'objet Race
				ConsanguinityBreed _breed = new ConsanguinityBreed().withId(_id).withName(_name)
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

	private ConsanguinityResponseObject buildFallbackConsanguinityList(int idClub) {

		List<ConsanguinityBreed> list = new ArrayList<ConsanguinityBreed>();
		list.add(new ConsanguinityBreed().withId(0));
		return new ConsanguinityResponseObject(list.size(), list);
	}

	private List<ConsanguinityVariety> extractVariety(List<BreederStatistics> _list) {

		List<ConsanguinityVariety> _varietyList = new ArrayList<ConsanguinityVariety>();
		int _id = 0;
		String _name = "";

		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		Map<TupleVariety, List<BreederStatistics>> _allVariety = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));

		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if (_allVariety.size() == 1
				&& StreamUtils.breedMonoVariety(_allVariety.keySet().stream().findFirst().get().getName()))
			return _varietyList;

		for (Map.Entry<TupleVariety, List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {

			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			// Moyenne des coef. de consanguinité
			double _cng = _currentVariety.getValue().stream().mapToDouble(BreederStatistics::getConsanguinite).average()
					.orElse(0.0);

			// Moyenne du nb d'ancètres communs
			List<ConsanguintyCommonAncestor> _commonAncestors = extractCommonAncestors(_currentVariety.getValue());

			// Création de l'objet Variety
			ConsanguinityVariety _variety = new ConsanguinityVariety().withId(_id).withName(_name)
					.withCng(format.format(Precision.round(_cng, 2))).withLitterByCommonAncestor(_commonAncestors);
			_varietyList.add(_variety);

		}

		return _varietyList;

	}

	private List<ConsanguintyCommonAncestor> extractCommonAncestors(List<BreederStatistics> _list) {

		List<ConsanguintyCommonAncestor> _commonAncestors = new ArrayList<ConsanguintyCommonAncestor>();

		Map<Integer, Integer> _breedAndCommonAncestor = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getNbAncetreCommun, Collectors.collectingAndThen(
						Collectors.mapping(BreederStatistics::getIdSaillie, Collectors.toSet()), Set::size)));

		// Remarque : la liste _breedAndCommonAncestor contient nombre d'ancêtres
		// communs, la liste des dossier
		// il faut maintenant compter de min à max (nb d'ancêtres commun), le nombre de
		// dossier
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
