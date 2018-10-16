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
import com.scc.lofselectclub.model.HealthStatistics;
import com.scc.lofselectclub.repository.ConfigurationClubRepository;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.repository.HealthRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleMaladie;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.breeder.BreederResponseObject;
import com.scc.lofselectclub.template.breeder.BreederVariety;
import com.scc.lofselectclub.template.breeder.BreederVarietyStatistics;
import com.scc.lofselectclub.template.health.HealthBreed;
import com.scc.lofselectclub.template.health.HealthBreedStatistics;
import com.scc.lofselectclub.template.health.HealthResponseObject;
import com.scc.lofselectclub.template.health.HealthResult;
import com.scc.lofselectclub.template.health.HealthTest;
import com.scc.lofselectclub.template.health.HealthType;
import com.scc.lofselectclub.template.health.HealthVariety;
import com.scc.lofselectclub.template.health.HealthVarietyStatistics;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.utils.TypeHealth;

import java.text.NumberFormat;
import java.util.ArrayList;
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
public class HealthService {

	private static final Logger logger = LoggerFactory.getLogger(HealthService.class);

	@Autowired
	private Tracer tracer;

	@Autowired
	private HealthRepository healthRepository;

	@Autowired
	private ConfigurationRaceRepository configurationRaceRepository;

	@Autowired
	private ConfigurationClubRepository configurationClubRepository;

	@Autowired
	ServiceConfig config;

	int idBreed = 0;
	int idVariety = 0;

	/**
	 * Retourne les données statistiques liées à la santé pour l'ensemble des races affiliées au club
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>HealthResponseObject</code>
	 * @throws EntityNotFoundException
	 */
	@HystrixCommand(fallbackMethod = "buildFallbackHealthList", threadPoolKey = "getStatistics", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "30"),
			@HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
					@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
					@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
					@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
							EntityNotFoundException.class })
	public HealthResponseObject getStatistics(int idClub) throws EntityNotFoundException {

		Span newSpan = tracer.createSpan("getStatistics");
		logger.debug("In the HealthService.getStatistics() call, trace id: {}",
				tracer.getCurrentSpan().traceIdString());

		String _name = "";

		List<ConfigurationClub> _breedsManagedByClub = new ArrayList<ConfigurationClub>(); 
		List<HealthBreed> _breeds = new ArrayList<HealthBreed>();
		Map<TupleBreed, Set<TupleVariety>> _varietyByBreed = new HashMap<TupleBreed, Set<TupleVariety>>();

		try {

			// Initialisation des données races / varietes associées au club
			_breedsManagedByClub = configurationClubRepository.findByIdClub(idClub);

			// Exception si le club n'a pas de races connues == l'id club n'existe pas
			if (_breedsManagedByClub.size() == 0)
				throw new EntityNotFoundException(BreederResponseObject.class, "idClub", String.valueOf(idClub));

			// Intialisation des races du club			
			_varietyByBreed = _breedsManagedByClub.stream()
					 .collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getLibelleRace()), 
                             Collectors.mapping( e -> new TupleVariety(e.getIdVariete(), e.getLibelleVariete()), Collectors.toSet())
                            )
					);

			// Lecture des races associées au club pour lesquelles des données ont été calculées
			Map<TupleBreed, List<HealthStatistics>> _allBreeds = healthRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
			for (Map.Entry<TupleBreed, List<HealthStatistics>> _currentBreed : _allBreeds.entrySet()) {

				int _year = 0;
				this.idBreed = _currentBreed.getKey().getId();
				_name = _currentBreed.getKey().getName();

				List<HealthBreedStatistics> _breedStatistics = new ArrayList<HealthBreedStatistics>();

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
				Map<Integer, List<HealthStatistics>> _breedGroupByYear = _currentBreed.getValue().stream()
						.filter(x -> x.getAnnee() >= minYear)
						.collect(StreamUtils.sortedGroupingBy(HealthStatistics::getAnnee));
				for (Map.Entry<Integer, List<HealthStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

					_year = _breedOverYear.getKey();

					// Suppression de l'année traitée
					_serieYear = ArrayUtils.removeElement(_serieYear, _year);

					// Lecture des résultats par catégorie soit pour une maladie type suivie, sous
					// surveillance, emergente, ou gène d'intérêt
					List<HealthType> _healthType = extractHealthTestType(_breedOverYear.getValue(), _referencedVarieties);

					HealthBreedStatistics _breed = new HealthBreedStatistics().withYear(_year)
							.withHealthType(_healthType);
					_breedStatistics.add(_breed);

				}

				// On finalise en initialisant les années pour lesquelles on a constaté une rupture
				for (int i = 0; i < _serieYear.length; i++) {
					HealthBreedStatistics _breed = new HealthBreedStatistics().withYear(_serieYear[i])
							.withHealthType(extractHealthTestType(new ArrayList<HealthStatistics>(), _referencedVarieties));
					_breedStatistics.add(_breed);
				}

				// Création de l'objet Race
				HealthBreed _breed = new HealthBreed().withId(this.idBreed).withName(_name).withStatistics(_breedStatistics);

				// Ajout à la liste
				_breeds.add(_breed);

			}

			// Réponse
			return new HealthResponseObject().withBreeds(_breeds).withSize(_breeds.size());

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
	 * @return			Objet <code>HealthResponseObject</code>
	 */
	private HealthResponseObject buildFallbackHealthList(int idClub) {

		List<HealthBreed> list = new ArrayList<HealthBreed>();
		list.add(new HealthBreed().withId(0));
		return new HealthResponseObject(list.size(), list);
	}

	/**
	 * Retourne par type d'examen, les données statistiques par maladie
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>healthType</code> de l'objet <code>HealthBreedStatistics</code>
	 * @see TypeHealth
	 */
	private List<HealthType> extractHealthTestType(List<HealthStatistics> _list, List<TupleVariety> _referencedVarieties) {

		List<HealthType> _resultByType = new ArrayList<HealthType>();

		Map<Integer, List<HealthStatistics>> _breedGroupByHealthType = _list.stream()
				.collect(StreamUtils.sortedGroupingBy(HealthStatistics::getNatureSuivi));
		for (Map.Entry<Integer, List<HealthStatistics>> _breedByHealthType : _breedGroupByHealthType.entrySet()) {

			// Lecture des resultats santé par maladie
			List<HealthTest> _test = extractHealthTest(_breedByHealthType.getValue(), _referencedVarieties);

			HealthType _type = new HealthType().withType(TypeHealth.fromId(_breedByHealthType.getKey()))
					.withHealthTest(_test);
			_resultByType.add(_type);
		}

		return _resultByType;
	}

	/**
	 * Retourne la liste des maladies pour un type de maladie
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>healthTest</code> de l'objet <code>HealthType</code>
	 */
	private List<HealthTest> extractHealthTest(List<HealthStatistics> _list, List<TupleVariety> _referencedVarieties) {

		List<HealthTest> _resultByType = new ArrayList<HealthTest>();

		Map<TupleMaladie, List<HealthStatistics>> _breedGroupByHealthResult = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleMaladie(r.getCodeMaladie(), r.getLibelleMaladie())));
		for (Map.Entry<TupleMaladie, List<HealthStatistics>> _breedByHealthTest : _breedGroupByHealthResult
				.entrySet()) {

			double _total = _breedByHealthTest.getValue().stream().map(e -> e.getNbResultat()).reduce(0,
					(x, y) -> x + y);

			// Lecture des résultats pour la maladie en cours
			List<HealthResult> _healthResults = extractHealthResult(_breedByHealthTest.getValue(), _total, _referencedVarieties);

			String _code = _breedByHealthTest.getKey().getCode();
			String _name = _breedByHealthTest.getKey().getName();

			HealthTest _i = new HealthTest().withCode(_code).withName(_name).withQtity((int) _total)
					.withHealthResults(_healthResults);

			_resultByType.add(_i);

		}

		return _resultByType;
	}

	/**
	 * Retourne la liste des résultats pour une maladie
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _total				Nombre de résultats santé pour le test
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>healthResults</code> de l'objet <code>HealthTest</code>
	 */
	private List<HealthResult> extractHealthResult(List<HealthStatistics> _list, double _total, List<TupleVariety> _referencedVarieties) {

		List<HealthResult> _resultByType = new ArrayList<HealthResult>();

		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		Map<TupleMaladie, List<HealthStatistics>> _breedGroupByHealthResult = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleMaladie(r.getCodeResultat(), r.getLibelleResultat())));
		for (Map.Entry<TupleMaladie, List<HealthStatistics>> _breedByResult : _breedGroupByHealthResult.entrySet()) {

			String _code = _breedByResult.getKey().getCode();
			String _name = _breedByResult.getKey().getName();

			int _qtity = _breedByResult.getValue().stream().map(e -> e.getNbResultat()).reduce(0, (x, y) -> x + y);
			double _percent = Precision.round((double) _qtity / _total, 2);

			List<HealthVariety> _variety = extractVariety(_breedByResult.getValue(), _qtity, _referencedVarieties);

			HealthResult _t = new HealthResult().withCode(_code).withName(_name).withQtity(_qtity)
					.withPercentage(format.format(_percent)).withVariety(_variety);

			_resultByType.add(_t);

		}

		return _resultByType;
	}

	/**
	 * Retourne les données statistiques pour l'ensemble des variétés de la race
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _total				Nombre de résultats santé pour le test
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return						Propriété <code>variety</code> de l'objet <code>HealthResult</code>
	 */
	private List<HealthVariety> extractVariety(List<HealthStatistics> _list, double _total, List<TupleVariety> _referencedVarieties) {

		List<HealthVariety> _varietyList = new ArrayList<HealthVariety>();
		this.idVariety = 0;
		String _name = "";

		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		Map<TupleVariety, List<HealthStatistics>> _allVariety = _list.stream()
				.collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));

		// Cas où la race est mono variété, la propriété n'est pas renseignée
		if (_referencedVarieties.size() == 1
				&& StreamUtils.breedMonoVariety(_referencedVarieties.stream().findFirst().orElse(new TupleVariety(0,""))) )
			return _varietyList;

		// On stocke la liste des variétés pour la race 
		List<TupleVariety> _varieties  = new ArrayList<TupleVariety>(_referencedVarieties);

		for (Map.Entry<TupleVariety, List<HealthStatistics>> _currentVariety : _allVariety.entrySet()) {

			this.idVariety = _currentVariety.getKey().getId();
			_name = _currentVariety.getKey().getName();

			int _qtity = _currentVariety.getValue().stream().map(e -> e.getNbResultat()).reduce(0, (x, y) -> x + y);
			double _percent = Precision.round((double) _qtity / _total, 2);

			// Création de l'objet VarietyStatistics
			HealthVarietyStatistics _varietyStatistics = new HealthVarietyStatistics().withQtity(_qtity)
					.withPercentage(format.format(_percent));

			// Création de l'objet Variety
			HealthVariety _variety = new HealthVariety().withId(this.idVariety).withName(_name).withStatistics(_varietyStatistics);
			_varietyList.add(_variety);

			// Suppression de la variété traitée
			_varieties.removeIf(e -> e.getId() == this.idVariety);
		}
		
		// Toutes les variétés n'ont pas fait l'objet d'une production doivent être mentionnées
		if (_varieties.size()>0) {
			for (TupleVariety v : _varieties) {
				HealthVariety _variety = new HealthVariety().withId(v.getId()).withName(v.getName())
						.withStatistics(new HealthVarietyStatistics().withQtity(0).withPercentage(format.format(0)));
				_varietyList.add(_variety);
			}
		}
		return _varietyList;

	}

}
