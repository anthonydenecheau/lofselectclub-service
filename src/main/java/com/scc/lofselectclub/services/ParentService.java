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
import com.scc.lofselectclub.template.breeder.BreederResponseObject;
import com.scc.lofselectclub.template.breeder.BreederVariety;
import com.scc.lofselectclub.template.breeder.BreederVarietyStatistics;
import com.scc.lofselectclub.template.parent.ParentVariety;
import com.scc.lofselectclub.utils.StreamUtils;
import com.scc.lofselectclub.utils.TypeHealth;
import com.scc.lofselectclub.utils.TypeRegistration;
import com.scc.lofselectclub.template.parent.ParentFather;
import com.scc.lofselectclub.template.parent.ParentBreed;
import com.scc.lofselectclub.template.parent.ParentResponseObject;
import com.scc.lofselectclub.template.parent.ParentBreedStatistics;
import com.scc.lofselectclub.template.parent.ParentCotation;
import com.scc.lofselectclub.template.parent.ParentFatherStatistics;
import com.scc.lofselectclub.template.parent.ParentFrequency;
import com.scc.lofselectclub.template.parent.ParentGender;
import com.scc.lofselectclub.template.parent.ParentRegisterType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
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
	private ConfigurationClubRepository configurationClubRepository;

	@Autowired
	ServiceConfig config;

	int limitTopN = 0;
	int idBreed = 0;
	int idVariety = 0;

	private Set<String> allTopN = new HashSet<String>();

	/**
	 * Retourne les données statistiques liées aux géniteurs pour l'ensemble des races affiliées au club
	 * 
	 * @param idClub	Identifiant du club
	 * @return			Objet <code>ParentResponseObject</code>
	 * @throws EntityNotFoundException
	 */
	@HystrixCommand(fallbackMethod = "buildFallbackParentList", threadPoolKey = "getStatistics", threadPoolProperties = {
			@HystrixProperty(name = "coreSize", value = "30"),
			@HystrixProperty(name = "maxQueueSize", value = "10") }, commandProperties = {
					@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
					@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "75"),
					@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "7000"),
					@HystrixProperty(name = "metrics.rollingStats.timeInMilliseconds", value = "15000"),
					@HystrixProperty(name = "metrics.rollingStats.numBuckets", value = "5") }, ignoreExceptions = {
							EntityNotFoundException.class })
	public ParentResponseObject getStatistics(int idClub) throws EntityNotFoundException {

		Span newSpan = tracer.createSpan("getStatistics");
		logger.debug("In the parentService.getStatistics() call, trace id: {}",
				tracer.getCurrentSpan().traceIdString());

		// topN etalon
		this.limitTopN = config.getLimitTopNFathers();

		String _name = "";

		List<ConfigurationClub> _breedsManagedByClub = new ArrayList<ConfigurationClub>(); 
		List<ParentBreed> _breeds = new ArrayList<ParentBreed>();
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
			Map<TupleBreed, List<BreederStatistics>> _allBreeds = breederRepository.findByIdClub(idClub).stream()
					.collect(Collectors.groupingBy(r -> new TupleBreed(r.getIdRace(), r.getNomRace())));
			for (Map.Entry<TupleBreed, List<BreederStatistics>> _currentBreed : _allBreeds.entrySet()) {

				List<ParentFatherStatistics> _fathersStatistics = new ArrayList<ParentFatherStatistics>();

				int _year = 0;
				this.idBreed = _currentBreed.getKey().getId();
				_name = _currentBreed.getKey().getName();

				List<ParentBreedStatistics> _breedStatistics = new ArrayList<ParentBreedStatistics>();

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
				
				// Recherche TopN Etalon s/ la race
				// Regle de sélection :
				// Pour chaque année, on sélectionne le top 20.
				// On en déduit les 100 meilleurs étalons (les doublons sont supprimés) qui vont
				// nous servir de base pour construire le classement s/ chaque année
				List<BreederStatistics> _topsNFathers = extractTopNFathers(minYear, _currentBreed.getValue());

				// Lecture des années (on ajoute un tri)
				Map<Integer, List<BreederStatistics>> _breedGroupByYear = _currentBreed.getValue().stream()
						.filter(x -> x.getAnnee() >= minYear)
						.collect(StreamUtils.sortedGroupingBy(BreederStatistics::getAnnee));
				for (Map.Entry<Integer, List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

					_year = _breedOverYear.getKey();

					// Suppression de l'année traitée
					_serieYear = ArrayUtils.removeElement(_serieYear, _year);

					List<Map<String, List<ParentGender>>> _origins = new ArrayList<Map<String, List<ParentGender>>>();
					Map<String, List<ParentGender>> _origin = new HashMap<String, List<ParentGender>>();

					List<ParentGender> _statsFather = extractFather(_breedOverYear.getValue());
					_origin.put("father", _statsFather);

					List<ParentGender> _statsMother = extractMother(_breedOverYear.getValue());
					_origin.put("mother", _statsMother);

					_origins.add(_origin);

					// Lecture des variétés s/ la race en cours (et pour l'année en cours)
					List<ParentVariety> _variety = extractVariety(_breedOverYear.getValue(), _referencedVarieties);

					// Lecture du nb de géniteur par cotations
					List<ParentCotation> _cotations = extractCotation(_breedOverYear.getValue());

					// Lecture de la fréquence d'utilisation du géniteur (uniquement étalon)
					List<ParentFrequency> _frequencies = extractFrequency(_breedOverYear.getValue());

					ParentBreedStatistics _breed = new ParentBreedStatistics().withYear(_year).withOrigins(_origins)
							.withCotations(_cotations).withFrequencies(_frequencies).withVariety(_variety);
					_breedStatistics.add(_breed);

					// Recherche TopN Etalon de l'année en cours s/ la race et sur les varietes
					List<ParentFather> _topsN = extractTopNOverYear(_year, _breedOverYear.getValue());
					ParentFatherStatistics _fatherTopN = new ParentFatherStatistics().withYear(_year)
							.withFathers(_topsN)
					// .withVariety(_topNVariety)
					;
					_fathersStatistics.add(_fatherTopN);

				}

				// On finalise en initialisant les années pour lesquelles on a constaté une rupture
				for (int i = 0; i < _serieYear.length; i++) {
					ParentBreedStatistics _breed = new ParentBreedStatistics().withYear(_serieYear[i])
							.withOrigins(new ArrayList<Map<String, List<ParentGender>>>())
							.withVariety(extractVariety(new ArrayList<BreederStatistics>(), _referencedVarieties));
					_breedStatistics.add(_breed);

					ParentFatherStatistics _fatherTopN = new ParentFatherStatistics().withYear(_serieYear[i])
							.withFathers(new ArrayList<ParentFather>())
					// .withVariety(_topNVariety)
					;
					_fathersStatistics.add(_fatherTopN);
				}

				// Création de l'objet Race
				ParentBreed _breed = new ParentBreed().withId(this.idBreed).withName(_name).withStatistics(_breedStatistics)
						.withTopN(_fathersStatistics);

				// Ajout à la liste
				_breeds.add(_breed);

			}

			// Réponse
			return new ParentResponseObject().withBreeds(_breeds).withSize(_breeds.size());

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
	 * @return			Objet <code>ParentResponseObject</code>
	 */
	private ParentResponseObject buildFallbackParentList(int idClub) {

		List<ParentBreed> list = new ArrayList<ParentBreed>();
		list.add(new ParentBreed().withId(0));
		return new ParentResponseObject(list.size(), list);
	}

	/**
	 * Retourne la répartition des étalons par type d'inscription
	 * 
	 * @param _list	Liste des données de production à analyser
	 * @return		Propriété <code>origins</code> de l'objet <code>ParentBreedStatistics</code>
	 * @see TypeRegistration
	 */
	private List<ParentGender> extractFather(List<BreederStatistics> _list) {

		List<ParentGender> _stats = new ArrayList<ParentGender>();

		int _qtity = 0;
		int _qtityTypeFrancais = 0;
		int _qtityTypeImport = 0;
		int _qtityTypeEtranger = 0;
		int _qtityTypeAutre = 0;

		try {

			// Male : Groupement par type d'inscription (sans doublons)
			Map<Integer, Integer> _map = _list.stream()
					.collect(Collectors.groupingBy(BreederStatistics::getTypeEtalon, Collectors.collectingAndThen(
							Collectors.mapping(BreederStatistics::getIdEtalon, Collectors.toSet()), Set::size)));

			for (Integer key : _map.keySet()) {
				Integer value = _map.get(key);

				switch (TypeRegistration.fromId(key)) {
				case FRANCAIS:
					_qtityTypeFrancais += value;
					break;
				case IMPORTES:
					_qtityTypeImport += value;
					break;
				case ETRANGERS:
					_qtityTypeEtranger += value;
					break;
				default:
					_qtityTypeAutre += value;
					break;
				}

				// total tous types confondus
				_qtity += value;
			}

			List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
			NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

			for (TypeRegistration s : TypeRegistration.values()) {
				int _qtityType = 0;
				double _percent = 0;
				switch (s) {
				case FRANCAIS:
					_qtityType = _qtityTypeFrancais;
					break;
				case IMPORTES:
					_qtityType = _qtityTypeImport;
					break;
				case ETRANGERS:
					_qtityType = _qtityTypeEtranger;
					break;
				default:
					_qtityType = _qtityTypeAutre;
					break;
				}

				_percent = Precision.round((double) _qtityType / (double) _qtity, 2);
				ParentRegisterType _type = new ParentRegisterType().withRegistration(s).withQtity(_qtityType)
						.withPercentage(format.format(_percent));
				_types.add(_type);
			}

			ParentGender _male = new ParentGender().withQtity(_qtity).withRegisterType(_types);
			_stats.add(_male);

		} finally {

		}

		return _stats;
	}

	/**
	 * Retourne la répartition des lices par type d'inscription
	 * 
	 * @param _list	Liste des données de production à analyser
	 * @return		Propriété <code>origins</code> de l'objet <code>ParentBreedStatistics</code>
	 * @see TypeRegistration
	 */
	private List<ParentGender> extractMother(List<BreederStatistics> _list) {

		List<ParentGender> _stats = new ArrayList<ParentGender>();

		int _qtity = 0;
		int _qtityTypeFrancais = 0;
		int _qtityTypeImport = 0;
		int _qtityTypeAutre = 0;

		try {

			// Femelle : Groupement par type d'inscription (sans doublons)
			Map<Integer, Integer> _map = _list.stream()
					.collect(Collectors.groupingBy(BreederStatistics::getTypeLice, Collectors.collectingAndThen(
							Collectors.mapping(BreederStatistics::getIdLice, Collectors.toSet()), Set::size)));

			for (Integer key : _map.keySet()) {
				Integer value = _map.get(key);

				switch (TypeRegistration.fromId(key)) {
				case FRANCAIS:
					_qtityTypeFrancais += value;
					break;
				case IMPORTES:
					_qtityTypeImport += value;
					break;
				default:
					_qtityTypeAutre += value;
					break;
				}

				// total tous types confondus
				_qtity += value;
			}

			List<ParentRegisterType> _types = new ArrayList<ParentRegisterType>();
			NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

			for (TypeRegistration s : TypeRegistration.values()) {
				int _qtityType = 0;
				double _percent = 0;
				switch (s) {
				case FRANCAIS:
					_qtityType = _qtityTypeFrancais;
					break;
				case IMPORTES:
					_qtityType = _qtityTypeImport;
					break;
				default:
					_qtityType = _qtityTypeAutre;
					break;
				}

				_percent = Precision.round((double) _qtityType / (double) _qtity, 2);
				ParentRegisterType _type = new ParentRegisterType().withRegistration(s).withQtity(_qtityType)
						.withPercentage(format.format(_percent));
				_types.add(_type);
			}

			ParentGender _female = new ParentGender().withQtity(_qtity).withRegisterType(_types);
			_stats.add(_female);

		} finally {

		}

		return _stats;
	}

	/**
	 * Retourne les données statistiques pour l'ensemble des variétés de la race
	 * 
	 * @param _list					Liste des données de production à analyser
	 * @param _referencedVarieties	Liste exhaustive des variétés pour la race lue
	 * @return 						Propriété <code>variety</code> de l'objet <code>ParentBreedStatistics</code>
	 */
	private List<ParentVariety> extractVariety(List<BreederStatistics> _list, List<TupleVariety> _referencedVarieties) {

		List<ParentVariety> _varietyList = new ArrayList<ParentVariety>();
		this.idVariety = 0;
		String _name = "";

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

			List<Map<String, List<ParentGender>>> _origins = new ArrayList<Map<String, List<ParentGender>>>();
			Map<String, List<ParentGender>> _origin = new HashMap<String, List<ParentGender>>();

			List<ParentGender> _statsFather = extractFather(_currentVariety.getValue());
			_origin.put("father", _statsFather);

			List<ParentGender> _statsMother = extractMother(_currentVariety.getValue());
			_origin.put("mother", _statsMother);

			_origins.add(_origin);

			// Lecture du nb de géniteur par cotations
			List<ParentCotation> _cotations = extractCotation(_currentVariety.getValue());

			// Lecture de la fréquence d'utilisation du géniteur (uniquement étalon)
			List<ParentFrequency> _frequencies = extractFrequency(_currentVariety.getValue());

			// Création de l'objet Variety
			ParentVariety _variety = new ParentVariety().withId(this.idVariety).withName(_name).withOrigins(_origins)
					.withCotations(_cotations).withFrequencies(_frequencies);
			_varietyList.add(_variety);
			
			// Suppression de la variété traitée
			_varieties.removeIf(e -> e.getId() == this.idVariety);
			
		}
		
		// Toutes les variétés n'ont pas fait l'objet d'une production doivent être mentionnées
		if (_varieties.size()>0) {
			for (TupleVariety v : _varieties) {
				ParentVariety _variety = new ParentVariety().withId(v.getId()).withName(v.getName())
						.withOrigins(new ArrayList<Map<String, List<ParentGender>>>())
						.withCotations(new ArrayList<ParentCotation>()).withFrequencies(new ArrayList<ParentFrequency>());
				_varietyList.add(_variety);
			}
		}		
		return _varietyList;
	}

	/**
	 * Retourne la fréquence d'utilisation d'un étalon dans les dossiers de portée
	 * 
	 * @param _list	Liste des données de production à analyser
	 * @return		Propriété <code>frequencies</code> de l'objet <code>ParentBreedStatistics</code>
	 */
	private List<ParentFrequency> extractFrequency(List<BreederStatistics> _list) {

		List<ParentFrequency> _frequencyList = new ArrayList<ParentFrequency>();

		TreeMap<Integer, Integer> _series = new TreeMap<Integer, Integer>();

		Map<Integer, Integer> _frequencyEtalon = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getIdEtalon, Collectors.collectingAndThen(
						Collectors.mapping(BreederStatistics::getIdSaillie, Collectors.toSet()), Set::size)));

		// Remarque : la liste _frequencyEtalon contient par étalon, le nombre de
		// saillie
		// il faut maintenant compter de min à max (nb de dossier), le nombre d'étalon
		// Rq: la demande est normalement : nb d'étalon utilisé pour 1 portée
		for (Map.Entry<Integer, Integer> _f : _frequencyEtalon.entrySet()) {
			// la serie n'existe pas, on l'initialise
			if (!_series.containsKey(_f.getValue()))
				_series.put(_f.getValue(), 1);
			else {
				_series.computeIfPresent(_f.getValue(), (k, v) -> v + 1);
			}

		}

		Integer highestKey = _series.lastKey();
		// Integer lowestKey = _series.firstKey();

		if (_series.size() > 0)
			// for (int i = 1; i <= highestKey; i++) {
			for (int i = 1; i <= 1; i++) {
				ParentFrequency c = null;
				if (_series.containsKey(i))
					c = new ParentFrequency().withTime(i).withQtity(_series.get(i));
				else
					c = new ParentFrequency().withTime(i).withQtity(0);

				_frequencyList.add(c);
			}

		return _frequencyList;
	}

	/**
	 * Retourne les données statistiques liées à la cotation des dossiers de portée
	 * 
	 * @param _list	Liste des données de production à analyser
	 * @return		Propriété <code>cotations</code> de l'objet <code>ParentBreedStatistics</code>
	 */
	private List<ParentCotation> extractCotation(List<BreederStatistics> _list) {

		List<ParentCotation> _cotationList = new ArrayList<ParentCotation>();
		int[] _cotReferences = new int[] { 1, 2, 3, 4, 5, 6 };
		NumberFormat format = NumberFormat.getPercentInstance(Locale.FRENCH);

		// Agregat pour les etalons puis les lices ...
		Map<Integer, Integer> _cotationsEtalon = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getCotationEtalon, Collectors.collectingAndThen(
						Collectors.mapping(BreederStatistics::getIdEtalon, Collectors.toSet()), Set::size)));
		Map<Integer, Integer> _cotationsLice = _list.stream()
				.collect(Collectors.groupingBy(BreederStatistics::getCotationLice, Collectors.collectingAndThen(
						Collectors.mapping(BreederStatistics::getIdLice, Collectors.toSet()), Set::size)));

		// ... Fusion des 2 Map
		_cotationsEtalon.forEach((k, v) -> _cotationsLice.merge(k, v, Integer::sum));

		double _total = _cotationsLice.values().stream().mapToInt(Number::intValue).sum();
		double _percent = 0;

		// Suppression de la cotation traitée
		for (Map.Entry<Integer, Integer> _cot : _cotationsLice.entrySet()) {

			_percent = Precision.round((double) _cot.getValue() / _total, 2);
			_cotReferences = ArrayUtils.removeElement(_cotReferences, _cot.getKey());
			ParentCotation c = new ParentCotation().withGrade(_cot.getKey()).withQtity((int) (long) _cot.getValue())
					.withPercentage(format.format(_percent));
			_cotationList.add(c);
		}

		for (int i : _cotReferences) {
			ParentCotation c = new ParentCotation().withGrade(i).withQtity(0).withPercentage(format.format(0));
			_cotationList.add(c);
		}

		_cotationList.sort(Comparator.comparing(ParentCotation::getGrade));

		return _cotationList;
	}

	// Voir Stackoverflow :
	// https://stackoverflow.com/questions/52343325/java-8-stream-how-to-get-top-n-count?noredirect=1#comment91673408_52343325
	// private List<ParentFather> extractTopNOverYear (int _limitTopN,
	// List<BreederStatistics> _list) {
	//
	// List<ParentFather> _topNFathers = new ArrayList<ParentFather>();
	//
	// Map<BreederStatistics, Long> counts = _list
	// .stream()
	// .collect(Collectors.groupingBy(x -> x, Collectors.counting()));
	//
	// Collection<BreederStatistics> _topN = StreamUtils.topN(counts, _limitTopN,
	// BreederStatistics::getIdEtalon);
	//
	// Map<String, Long> _resume = _topN
	// .stream()
	// .collect(
	// Collectors.groupingBy(
	// BreederStatistics::getNomEtalon, Collectors.counting()
	// )
	// );
	//
	// for (Map.Entry<String, Long> entry : _resume.entrySet()) {
	// ParentFather _currentEtalon = new ParentFather()
	// .withName(entry.getKey())
	// .withQtity((int) (long)entry.getValue())
	// ;
	// _topNFathers.add(_currentEtalon);
	// }
	//
	//
	// return _topNFathers;
	// }

	/**
	 * Retourne le classement des étalons ayant produit le plus de portées dans l'année
	 * 
	 * @param _year	Année
	 * @param _list	Liste des données de production à analyser
	 * @return		Propriété <code>fathers</code> de l'objet <code>ParentFatherStatistics</code>
	 */
	private List<ParentFather> extractTopNOverYear(int _year, List<BreederStatistics> _list) {

		List<ParentFather> _topNFathers = new ArrayList<ParentFather>();

		try {

			// 1. On groupe les étalons par qtites pour l'année en cours
			Map<String, Long> _affixes = _list.stream().filter(x -> (_year == x.getAnnee()))
					.collect(Collectors.groupingBy(BreederStatistics::getNomEtalon, Collectors.counting()));
			;

			// 3. On complète par les étalons potentiellement manquants
			boolean g = false;
			for (String s : this.allTopN) {
				g = false;
				for (Map.Entry<String, Long> entry : _affixes.entrySet()) {
					if (s.equals(entry.getKey())) {
						g = true;
						break;
					}
				}

				if (!g) {
					ParentFather _currentEtalon = new ParentFather().withName(s).withQtity(0);
					_topNFathers.add(_currentEtalon);
				}
			}

			// 2. On alimente notre Map
			for (Entry<String, Long> _father : _affixes.entrySet()) {
				ParentFather _currentFather = new ParentFather().withName(_father.getKey())
						.withQtity((int) (long) _father.getValue());
				_topNFathers.add(_currentFather);
			}

			// 4. On trie les résultats par quantites décroissante
			_topNFathers.sort(Collections.reverseOrder(Comparator.comparing(ParentFather::getQtity)));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return _topNFathers;

	}

	/**
	 * Construction de la liste des étalons qui ont le plus produit sur les 5 dernières années
	 * 
	 * @param minYear	Année plancher	
	 * @param _list		Liste des données de production à analyser
	 * @return			Liste des étalons avec le nombre de portées
	 */
	private List<BreederStatistics> extractTopNFathers(int minYear, List<BreederStatistics> _list) {

		List<BreederStatistics> _topNFathers = new ArrayList<BreederStatistics>();
		Set<String> _sortedFathers = new HashSet<String>();

		// Sélection de l'année
		Map<Integer, List<BreederStatistics>> _breedGroupByYear = _list.stream().filter(x -> x.getAnnee() >= minYear)
				.collect(StreamUtils.sortedGroupingBy(BreederStatistics::getAnnee));
		for (Map.Entry<Integer, List<BreederStatistics>> _breedOverYear : _breedGroupByYear.entrySet()) {

			// 1. On groupe les étalons par qtites 
			Map<BreederStatistics, Long> _bestOfFathersOverYear = _breedOverYear.getValue().stream()
					.collect(Collectors.groupingBy(prd -> new BreederStatistics(prd.getIdEtalon(), prd.getNomEtalon()),
							Collectors.counting()));
			;

			// 2. On ne conserve que les 20 meilleurs que l'on ajouté à notre liste
			// existante (l'objet Set nous prémunit des doublons)
			_sortedFathers.addAll(_bestOfFathersOverYear.entrySet().stream()
					.sorted(Map.Entry.comparingByValue(Collections.reverseOrder())).limit(this.limitTopN)
					.map(a -> a.getKey().getNomEtalon()).collect(Collectors.toSet()));

		}

		// On conserve le topN étalon (null-safe way)
		this.allTopN = Optional.ofNullable(_sortedFathers).map(Set::stream).orElseGet(Stream::empty)
				.collect(Collectors.toSet());

		// 3. On (re)construit la liste qui sera utilisée pour la lecture des filtres
		// par année et/ou par mois.
		_topNFathers = _list.stream().filter(x -> _sortedFathers.contains(x.getNomEtalon()))
				.collect(Collectors.toList());

		return _topNFathers;
	}
}
