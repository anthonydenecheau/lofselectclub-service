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

	int idBreed = 0;
	int idVariety = 0;
	
	/**
	 * Retourne les données statistiques liées aux naissances pour l'ensemble des races affiliées au club
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>BirthResponseObject</code>
	 * @throws EntityNotFoundException
	 */	
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

		String _name = "";

		List<ConfigurationClub> _breedsManagedByClub = new ArrayList<ConfigurationClub>(); 
		List<BirthBreed> _breeds = new ArrayList<BirthBreed>();
		Map<TupleBreed, Set<TupleVariety>> _varietyByBreed = new HashMap<TupleBreed, Set<TupleVariety>>();

		try {

			// Initialisation des données races / varietes associées au club
			_breedsManagedByClub = configurationClubRepository.findByIdClub(idClub);
			
			// Exception si le club n'a pas de races connues == l'id club n'existe pas
			if (_breedsManagedByClub.size() == 0)
				throw new EntityNotFoundException(BirthResponseObject.class, "idClub", String.valueOf(idClub));

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

				List<BirthBreedStatistics> _breedStatistics = new ArrayList<BirthBreedStatistics>();

				// Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
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

					// Somme des chiots males, femelles, portée
					BreederStatistics sumBirth = _breedOverYear.getValue().stream().reduce(new BreederStatistics(0, 0),
							(x, y) -> {
								return new BreederStatistics(x.getNbMale() + y.getNbMale(),
										x.getNbFemelle() + y.getNbFemelle());
							});

					long _qtity = _breedOverYear.getValue().stream().collect(Collectors.counting());

					double prolificity = _breedOverYear.getValue().stream().findFirst()
							.map(BreederStatistics::getProlificiteRace).orElse(0.0);

					// Lecture des cotations des portées s/ la race en cours (et pour l'année en cours)
					List<BirthCotation> _cotations = extractCotation(_breedOverYear.getValue());

					// Lecture des variétés s/ la race en cours (et pour l'année en cours)
					List<BirthVariety> _variety = extractVariety(_breedOverYear.getValue(), _referencedVarieties);

					BirthBreedStatistics _breed = new BirthBreedStatistics().withYear(_year)
							.withNumberOfMale(sumBirth.getNbMale()).withNumberOfFemale(sumBirth.getNbFemelle())
							.withNumberOfPuppies(sumBirth.getNbMale() + sumBirth.getNbFemelle())
							.withTotalOfLitter((int) (long) _qtity).withProlificity(Precision.round(prolificity, 2))
							.withVariety(_variety).withCotations(_cotations);
					_breedStatistics.add(_breed);

				}

				// On finalise en initialisant les années pour lesquelles on a constaté une rupture
				for (int i = 0; i < _serieYear.length; i++) {
					BirthBreedStatistics _breed = new BirthBreedStatistics().withYear(_serieYear[i]).withNumberOfMale(0)
							.withNumberOfFemale(0).withNumberOfPuppies(0).withTotalOfLitter(0).withProlificity(0)
							.withVariety(extractVariety(new ArrayList<BreederStatistics>(), _referencedVarieties)).withCotations(extractCotation(new ArrayList<BreederStatistics>()));
					_breedStatistics.add(_breed);
				}

				// Création de l'objet Race
				BirthBreed _breed = new BirthBreed().withId(this.idBreed).withName(_name).withStatistics(_breedStatistics);

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

	/**
	 * Fonction fallbackMethod de la fonction principale <code>getStatistics</code> (Hystrix Latency / Fault Tolerance)
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>BirthResponseObject</code>
	 */
	private BirthResponseObject buildFallbackBirthList(int idClub) {

		List<BirthBreed> list = new ArrayList<BirthBreed>();
		list.add(new BirthBreed().withId(0));
		return new BirthResponseObject(list.size(), list);
	}

	/**
	 * Retourne la répartition du nombre de portées par cotation
	 * 
	 * @param _list	Liste des données de production à analyser
	 * @return		Propriété <code>cotations</code> de l'objet <code>BirthBreedStatistics</code>
	 */
	private List<BirthCotation> extractCotation(List<BreederStatistics> _list) {

		List<BirthCotation> _cotationList = new ArrayList<BirthCotation>();
		int[] _cotReferences = new int[] { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		Map<Integer, Long> _cotations = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getCotationPortee, Collectors.counting()));

		double _total = _cotations.values().stream().mapToInt(Number::intValue).sum();
		double _percent = 0;

		for (Map.Entry<Integer, Long> _cot : _cotations.entrySet()) {

			_percent = Precision.round((double) _cot.getValue() / _total, 2);
			// Suppression de la cotation traitée
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

	/**
	 * Retourne les données statistiques pour l'ensemble des variétés de la race
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>variety</code> de l'objet <code>BirthBreedStatistics</code>
	 */
	private List<BirthVariety> extractVariety(List<BreederStatistics> _list, List<TupleVariety> _referencedVarieties) {

		List<BirthVariety> _varietyList = new ArrayList<BirthVariety>();
		this.idVariety = 0;
		String _name = "";

		Map<TupleVariety, List<BreederStatistics>> _allVariety = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));

		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if (_referencedVarieties.size() == 1
				&& StreamUtils.breedMonoVariety(_referencedVarieties.stream().findFirst().orElse(new TupleVariety(0,""))))
			return _varietyList;

		// On stocke la liste des variétés pour la race 
		List<TupleVariety> _varieties  = new ArrayList<TupleVariety>(_referencedVarieties);

		for (Map.Entry<TupleVariety, List<BreederStatistics>> _currentVariety : _allVariety.entrySet()) {

			this.idVariety = _currentVariety.getKey().getId();
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
			BirthVariety _variety = new BirthVariety().withId(this.idVariety).withName(_name)
					.withNumberOfMale(sumBirth.getNbMale()).withNumberOfFemale(sumBirth.getNbFemelle())
					.withNumberOfPuppies(sumBirth.getNbMale() + sumBirth.getNbFemelle())
					.withTotalOfLitter((int) (long) _qtity).withProlificity(Precision.round(prolificity, 2))
					.withCotations(_cotations);
			_varietyList.add(_variety);
			
			// Suppression de la variété traitée
			_varieties.removeIf(e -> e.getId() == this.idVariety);

		}

		// Toutes les variétés n'ont pas fait l'objet d'une production doivent être mentionnées
		if (_varieties.size()>0) {
			for (TupleVariety v : _varieties) {
				BirthVariety _variety = new BirthVariety().withId(v.getId()).withName(v.getName())
						.withNumberOfMale(0).withNumberOfFemale(0)
						.withNumberOfPuppies(0)
						.withTotalOfLitter(0)
						.withCotations(extractCotation(new ArrayList<BreederStatistics>()));
				_varietyList.add(_variety);
			}
		}
		return _varietyList;

	}
}
