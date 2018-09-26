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
import com.scc.lofselectclub.template.birth.BirthBreed;
import com.scc.lofselectclub.template.birth.BirthResponseObject;
import com.scc.lofselectclub.template.birth.BirthVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.template.birth.BirthBreedStatistics;
import com.scc.lofselectclub.template.birth.BirthCotation;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Precision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class BirthService {

	private static final Logger logger = LoggerFactory.getLogger(BirthService.class);

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

	@HystrixCommand(fallbackMethod = "buildFallbackBirthList", threadPoolKey = "getStatistics", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "30"),
			@HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
					@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
					@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
					@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
							EntityNotFoundException.class })
	public BirthResponseObject getStatistics(int idClub) throws EntityNotFoundException {

		Span newSpan = tracer.createSpan("getStatistics");
		logger.debug("In the birthService.getStatistics() call, trace id: {}", tracer.getCurrentSpan().traceIdString());

		int _id = 0;
		String _name = "";

		List<BirthBreed> _breeds = new ArrayList<BirthBreed>();
		Map<Integer, Set<Integer>> _varietyByBreed = new HashMap<Integer, Set<Integer>>();

		try {

			// Initialisation des données races / varietes associées au club
			_varietyByBreed = configurationClubRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(ConfigurationClub::getIdRace,
							Collectors.mapping(ConfigurationClub::getIdVariete, Collectors.toSet())));

			// Exception si le club n'a pas de races connues == l'id club n'existe pas
			if (_varietyByBreed.size() == 0)
				throw new EntityNotFoundException(BirthResponseObject.class, "idClub", String.valueOf(idClub));

			// Lecture des races associées au club pour lesquelles des données ont été
			// calculées
			Map<TupleBreed, List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
			for (Map.Entry<TupleBreed, List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {

				int _year = 0;
				_id = _currentBreed.getKey().getId();
				_name = _currentBreed.getKey().getName();

				List<BirthBreedStatistics> _breedStatistics = new ArrayList<BirthBreedStatistics>();

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

					// Somme des chiots males, femelles, portée
					BreederStatistics sumBirth = _breedOverYear.getValue().stream().reduce(new BreederStatistics(0, 0),
							(x, y) -> {
								return new BreederStatistics(x.getNbMale() + y.getNbMale(),
										x.getNbFemelle() + y.getNbFemelle());
							});

					long _qtity = _breedOverYear.getValue().stream().collect(Collectors.counting());

					double prolificity = _breedOverYear.getValue().stream().findFirst()
							.map(BreederStatistics::getProlificiteRace).orElse(0.0);

					// Lecture des cotations des portées s/ la race en cours (et pour l'année en
					// cours)
					List<BirthCotation> _cotations = extractCotation(_breedOverYear.getValue());

					// Lecture des variétés s/ la race en cours (et pour l'année en cours)
					List<BirthVariety> _variety = extractVariety(_breedOverYear.getValue());

					BirthBreedStatistics _breed = new BirthBreedStatistics().withYear(_year)
							.withNumberOfMale(sumBirth.getNbMale()).withNumberOfFemale(sumBirth.getNbFemelle())
							.withNumberOfPuppies(sumBirth.getNbMale() + sumBirth.getNbFemelle())
							.withTotalOfLitter((int) (long) _qtity).withProlificity(Precision.round(prolificity, 2))
							.withVariety(_variety).withCotations(_cotations);
					_breedStatistics.add(_breed);

				}

				// On finalise en initialisant les années pour lesquelles on a constaté une
				// rupture
				for (int i = 0; i < _serieYear.length; i++) {
					BirthBreedStatistics _breed = new BirthBreedStatistics().withYear(_serieYear[i]).withNumberOfMale(0)
							.withNumberOfFemale(0).withNumberOfPuppies(0).withTotalOfLitter(0).withProlificity(0)
							.withVariety(new ArrayList<BirthVariety>()).withCotations(new ArrayList<BirthCotation>());
					_breedStatistics.add(_breed);
				}

				// Création de l'objet Race
				BirthBreed _breed = new BirthBreed().withId(_id).withName(_name).withStatistics(_breedStatistics);

				// Ajout à la liste
				_breeds.add(_breed);

			}

			// Réponse
			return new BirthResponseObject().withBreeds(_breeds).withSize(_breeds.size());

		} finally {
			newSpan.tag("peer.service", "postgres");
			newSpan.logEvent(org.springframework.cloud.sleuth.Span.CLIENT_RECV);
			tracer.close(newSpan);
		}
	}

	private BirthResponseObject buildFallbackBirthList(int idClub) {

		List<BirthBreed> list = new ArrayList<BirthBreed>();
		list.add(new BirthBreed().withId(0));
		return new BirthResponseObject(list.size(), list);
	}

	private List<BirthCotation> extractCotation(List<BreederStatistics> _list) {

		List<BirthCotation> _cotationList = new ArrayList<BirthCotation>();
		int[] _cotReferences = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		Map<Integer, Long> _cotations = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getCotationPortee, Collectors.counting()));
		;

		double _total = _cotations.values().stream().mapToInt(Number::intValue).sum();
		double _percent = 0;

		// Suppression de la cotation traitée
		for (Map.Entry<Integer, Long> _cot : _cotations.entrySet()) {

			_percent = Precision.round((double) _cot.getValue() / _total, 2);
			_cotReferences = ArrayUtils.removeElement(_cotReferences, _cot.getKey());
			BirthCotation c = new BirthCotation().withGrade(_cot.getKey()).withQtity((int) (long) _cot.getValue())
					.withPercentage(format.format(_percent));
			_cotationList.add(c);
		}

		for (int i : _cotReferences) {
			BirthCotation c = new BirthCotation().withGrade(i).withQtity(0).withPercentage(format.format(0));
			_cotationList.add(c);
		}

		_cotationList.sort(Comparator.comparing(BirthCotation::getGrade));

		return _cotationList;
	}

	private List<BirthVariety> extractVariety(List<BreederStatistics> _list) {

		List<BirthVariety> _varietyList = new ArrayList<BirthVariety>();
		int _id = 0;
		String _name = "";

		Map<TupleVariety, List<BreederStatistics>> _allVariety = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));

		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if (_allVariety.size() == 1
				&& StreamUtils.breedMonoVariety(_allVariety.keySet().stream().findFirst().get().getName()))
			return _varietyList;

		for (Map.Entry<TupleVariety, List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {

			_id = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			// Somme des chiots males, femelles, portée
			BreederStatistics sumBirth = _currentVariety.getValue().stream().reduce(new BreederStatistics(0, 0),
					(x, y) -> {
						return new BreederStatistics(x.getNbMale() + y.getNbMale(),
								x.getNbFemelle() + y.getNbFemelle());
					});

			long _qtity = _currentVariety.getValue().stream().collect(Collectors.counting());

			double prolificity = _currentVariety.getValue().stream().findFirst()
					.map(BreederStatistics::getProlificiteVariete).orElse(0.0);

			// Lecture des cotations des portée
			List<BirthCotation> _cotations = extractCotation(_currentVariety.getValue());

			// Création de l'objet Variety
			BirthVariety _variety = new BirthVariety().withId(_id).withName(_name)
					.withNumberOfMale(sumBirth.getNbMale()).withNumberOfFemale(sumBirth.getNbFemelle())
					.withNumberOfPuppies(sumBirth.getNbMale() + sumBirth.getNbFemelle())
					.withTotalOfLitter((int) (long) _qtity).withProlificity(Precision.round(prolificity, 2))
					.withCotations(_cotations);
			_varietyList.add(_variety);

		}

		return _varietyList;

	}
}
