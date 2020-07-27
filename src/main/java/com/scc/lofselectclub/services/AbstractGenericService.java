package com.scc.lofselectclub.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IntSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;

import com.scc.lofselectclub.exceptions.EntityNotFoundException;
import com.scc.lofselectclub.model.GenericStatistics;
import com.scc.lofselectclub.model.ParametersVariety;
import com.scc.lofselectclub.model.SerieDefinition;
import com.scc.lofselectclub.model.SerieHeight;
import com.scc.lofselectclub.model.ConfigurationClub;
import com.scc.lofselectclub.model.ConfigurationRace;
import com.scc.lofselectclub.model.ConfirmationStatistics;
import com.scc.lofselectclub.repository.ConfigurationClubRepository;
import com.scc.lofselectclub.repository.ConfigurationRaceRepository;
import com.scc.lofselectclub.repository.SerieDefinitionRepository;
import com.scc.lofselectclub.template.TupleBreed;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.utils.StreamUtils;

public abstract class AbstractGenericService<T, U> {

   private static final Logger logger = LoggerFactory.getLogger(AbstractGenericService.class);
         
   public AbstractGenericService() {
      super();
   }
   
   @Autowired
   protected ConfigurationRaceRepository configurationRaceRepository;

   @Autowired
   protected SerieDefinitionRepository rangeDefinitionRepository;
   
   @Autowired
   protected ConfigurationClubRepository configurationClubRepository;

   protected T genericTemplate;
   protected void setGenericTemplate(T genericTemplate) {
      this.genericTemplate = genericTemplate;
   }
   protected T getGenericTemplate() {
      return genericTemplate;
   }
   
   protected Class<U> type;
   protected Class<U> getType() {
      return type;
   }
   protected void setType(Class<U> type) {
      this.type = type;
   }

   protected List<SerieDefinition> _serieQtity = new ArrayList<SerieDefinition>(); 
   protected Map<TupleBreed, LinkedHashSet<TupleVariety>> _varietyByBreed = new LinkedHashMap<TupleBreed, LinkedHashSet<TupleVariety>>();
   protected List<TupleVariety> _referencedVarieties = new ArrayList<TupleVariety>();
   protected int[] _serieYear;
   protected String _period = "";
   protected int _idBreed = 0;
   protected String _nameBreed = "";
   protected int _idVariety = 0;
   protected String _nameVariety = "";
   protected boolean _mandatoryHeight = false;
   protected List<SerieHeight> _serieHeight = null;

   /**
    * Construit la liste des races ainsi que la (ou les) variété(s) affiliée(s) et dont le club à la charge
    * 
    * @param idClub  Identifiant du club
    * @throws EntityNotFoundException
    */
   protected void setClubBreedData(int idClub) throws EntityNotFoundException {

      List<ConfigurationClub> _breedsManagedByClub = new ArrayList<ConfigurationClub>();

      try { 
         
         // Initialisation des données races / varietes associées au club
         try {
            _breedsManagedByClub = configurationClubRepository.findByIdClub(idClub, orderByTri());
         } catch (Exception e) {
            logger.error("setClubBreedData {}", e.getMessage());
         }
   
         // Exception si le club n'a pas de races connues == l'id club n'existe pas
         if (_breedsManagedByClub.size() == 0)
            throw new EntityNotFoundException(getGenericTemplate().getClass(), "idClub", String.valueOf(idClub));
   
         // Intialisation des races du club
         this._varietyByBreed = _breedsManagedByClub.stream()
               .collect(Collectors.groupingBy(
                     r -> new TupleBreed(r.getIdRace(), r.getNomRace())
                     , LinkedHashMap::new
                     , Collectors.mapping(e -> new TupleVariety(e.getIdVariete(), e.getNomVariete()),Collectors.toCollection(LinkedHashSet::new))));
      
      } finally {
      }
      
   }
   
   /**
    * Création d'un objet Sort
    *    Le tri se fait par le n° d'ordre d'édition de la variété
    * 
    */
   protected Sort orderByTri() {
      return new Sort(Sort.Direction.ASC, "tri");
   }
   

   /**
    * Construit la liste exhaustive des variétés pour la race lue
    * 
    * @param idBreed Identifiant de la race
    */
   protected void setVarietiesByIdBreed(int idBreed) {
      this._referencedVarieties = this._varietyByBreed.entrySet()
            .stream()
            .filter(r -> r.getKey().getId() == idBreed)
            .flatMap(map -> map.getValue().stream())
            .collect(Collectors.toList());
   }

   /**
    * Contruction de la série des quantités
    * 
    * @param idBreed Identifiant de la race
    */
   protected void setQtitySeries(int idBreed) {
      
      try {
         ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(idBreed);
         this._serieQtity = rangeDefinitionRepository
               .findByIdSerieGroupOrderBySequence(_configurationRace.getIdSerieGroup());
      } catch (Exception e) {
         logger.error("setQtitySeries : {}",e.getMessage());
      } finally {
      } 
      
   }
   
   /**
    * Contruction de la série des années
    * 
    * @param idBreed Identifiant de la race
    */
   protected void setYearSeries(int idBreed) {
      
      try {
         ConfigurationRace _configurationRace = configurationRaceRepository.findByIdRace(idBreed);
         this._serieYear = StreamUtils.findSerieYear(_configurationRace.getLastDate());
         this._period = _configurationRace.getBreakPeriod();
      } catch (Exception e) {
         logger.error("setYearSeries : {}",e.getMessage());
      } finally {
      }      
      
   }
   
   /**
    * Contruction de l'objet variety
    * 
    * @return  true si les variétés doivent être affichées 
    */
   protected boolean isPopulateVarieties() {
      if (this._referencedVarieties.size() == 1 && StreamUtils
            .breedMonoVariety(this._referencedVarieties.stream().findFirst().orElse(new TupleVariety(0, ""))))
         return false;
     
      return true;
   }
   
   /**
    * Lecture des données statistiques regroupées par variétés (on doit préserver le tri s/ les variétés)
    * 
    * @param _list   Liste des données statistiques
    * @return        LinkedHashMap<TupleVariety, List<?>>
    */
   @SuppressWarnings("unchecked")
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M  getVarietyStatistics (List<? extends GenericStatistics> _list) {
      return
            (M) _list.stream()
               .collect(Collectors.groupingBy(
                     r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())
                     , LinkedHashMap::new
                     , Collectors.toList())
                     );
      
   }
   
//   @SuppressWarnings("unchecked")
//   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M  getVarietyStatistics (List<? extends GenericStatistics> _list) {
//      return
//            (M) _list.stream()
//               .collect(Collectors.groupingBy(r -> new TupleVariety(r.getIdVariete(), r.getNomVariete())));
//   }

   /**
    * Lecture des données statistiques regroupées par races qui s'appuie s/ la table ls_stats_eleveur
    * 
    * @param idClub  Identifiant du club
    * @return        Map<TupleBreed, List<?>>
    */ 
   protected abstract <K, V, C extends Collection<V>, M extends Map<K, C>> M  getDataStatistics (int idClub);
   
   /**
    * Lecture des données statistiques regroupées par année
    * 
    * @param _year   Année planchée
    * @param _list   Liste des données statistiques de la race
    * @return        Map<Integer, List<?>>
    */
   @SuppressWarnings("unchecked")
   protected <K, V, C extends Collection<V>, M extends Map<K, C>> M  getYearStatistics (List<? extends GenericStatistics> _list) {
      return 
            (M)_list.stream()
            .filter(x -> x.getAnnee() >= this._serieYear[0])
            .collect(StreamUtils.sortedGroupingBy(x -> x.getAnnee()));      
   }
   
   /**
    * Retourne les données statistiques pour l'ensemble des variétés de la race
    * 
    * @param _list   Liste des données de production à analyser
    * @return        Propriété <code>variety</code> de la classe fille de <code>GenericStatistics</code>
    */
   @SuppressWarnings("hiding")
   protected <T> List<T> populateVarieties(List<? extends GenericStatistics> _list, ParametersVariety _parameters) {

      this._idVariety = 0;
      this._nameVariety = "";
      List<T> _varietyList = new ArrayList<T>();

      try {
         
         // Cas où la race est mono variété, la propriété n'est pas renseignée
         if (!isPopulateVarieties())
            return _varietyList;
         
         // On stocke la liste des variétés pour la race
         List<TupleVariety> _varieties = new ArrayList<TupleVariety>(this._referencedVarieties);
   
         // Lecture des variétés associées à la race pour lesquelles des données ont été calculées
         Map<TupleVariety, List<T>> _allVariety = getVarietyStatistics(_list);
         for (Map.Entry<TupleVariety, List<T>> _currentVariety : _allVariety.entrySet()) {
   
            this._idVariety = _currentVariety.getKey().getId();
            this._nameVariety = _currentVariety.getKey().getName();
            
            _varietyList.add(
                  readVariety(_currentVariety.getValue(), _parameters)
             );
            
            // Suppression de la variété traitée
            _varieties.removeIf(e -> e.getId() == this._idVariety);
         }
         
         // Toutes les variétés n'ont pas fait l'objet d'une production doivent être mentionnées
         if (_varieties.size() > 0) {
               for (TupleVariety v : _varieties) {
                  _varietyList.add(
                        emptyVariety(v, _parameters)
                   );
            }
         }

      } catch (Exception e) {
         logger.error("populateVarieties : {}",e.getMessage());
      } finally {
      }   
      
      return _varietyList;

   }
   
   @SuppressWarnings("hiding")
   protected abstract <T> T readVariety(List<T> _stats, ParametersVariety _parameters);
   
   @SuppressWarnings("hiding")
   protected abstract <T> T emptyVariety(TupleVariety _variety, ParametersVariety _parameters);

   /**
    * Caste la liste générique en une liste typée
    * 
    * @param _stats  Données de production générique
    * @return        Liste de type <code>U</code>
    */
   protected List<U> feed(List<? extends GenericStatistics> _stats) {
      // Cf. https://www.baeldung.com/java-type-casting
      List<U> _list = new ArrayList<U>();
      _stats.forEach(x -> {
          if (type.isInstance(x)) {
             U objAsType = type.cast(x);
             _list.add(objAsType);
          }
      });
      return _list;
   }
   
   /**
    * Retourne les données statistiques de la race pour les années paramétrées
    * 
    * @param _list   Données de production générique pour une race donnée
    * @return         
    */
   @SuppressWarnings("hiding")
   protected <T> List<T> populateYears(List<? extends GenericStatistics> _list) {
      
      int _year = 0;
      List<T> _breedYearStatistics = new ArrayList<T>();

      try {
         // Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
         // A voir si les années précédentes ne feront pas l'objet d'une suppression côté
         // data (BdD); auquel cas, ce code sera obsolète
         setYearSeries(this._idBreed);
   
         // Lecture des variétés référencées
         // si une variété n'est pas représentée pour l'année, il faut l'ajouter avec qtity = 0
         setVarietiesByIdBreed(this._idBreed);
         
         // Pour le WS confirmation, on relève l'intégralité des tailles (quand elle est obligatoire)
         if (this.getType().getClass().isInstance(ConfirmationStatistics.class) && (this._mandatoryHeight)) {
            populateHeightSeries(_list);
         }
         
         Map<Integer, List<T>> _breedGroupByYear = getYearStatistics(_list);
         for (Map.Entry<Integer, List<T>> _breedOverYear : _breedGroupByYear.entrySet()) {
            
            _year = _breedOverYear.getKey();
   
            // Suppression de l'année traitée
            this._serieYear = ArrayUtils.removeElement(this._serieYear, _year);
            
            _breedYearStatistics.add(
                  readYear(_breedOverYear.getValue(), _year)
             );
            
         }
         
         // On finalise en initialisant les années pour lesquelles on a constaté une rupture
         for (int i = 0; i < this._serieYear.length; i++) {
            _breedYearStatistics.add(
                  emptyYear(this._serieYear[i])
             );
         }

      } catch (Exception e) {
         logger.error("populateYears : {}",e.getMessage());
      } finally {
      } 
      
      return _breedYearStatistics;
      
   }
   
   @SuppressWarnings("hiding")
   protected abstract <T> T readYear(List<T> _stats, int _year);

   @SuppressWarnings("hiding")
   protected abstract <T> T emptyYear(int _year);

   /**
    * Retourne le top N pour une race et une année donnée
    * 
    * @param _list   Données de production générique pour une liste d'affixe/d'étalon
    * @return
    */
   @SuppressWarnings("hiding")
   protected <T> List<T> populateTopN(List<? extends GenericStatistics> _list) {

      int _year = 0;
      List<T> _topNStatistics = new ArrayList<T>();

      try {
         // Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
         // A voir si les années précédentes ne feront pas l'objet d'une suppression côté
         // data (BdD); auquel cas, ce code sera obsolète
         setYearSeries(this._idBreed);
   
         // Lecture des variétés référencées
         // si une variété n'est pas représentée pour l'année, il faut l'ajouter avec qtity = 0
         setVarietiesByIdBreed(this._idBreed);
         
         Map<Integer, List<T>> _breedGroupByYear = getYearStatistics(_list);
         for (Map.Entry<Integer, List<T>> _breedOverYear : _breedGroupByYear.entrySet()) {
            
            _year = _breedOverYear.getKey();
   
            // Suppression de l'année traitée
            this._serieYear = ArrayUtils.removeElement(this._serieYear, _year);
            
            _topNStatistics.add(
                  readTopN(_breedOverYear.getValue(), _year)
            );
            
         }
         
         // On finalise en initialisant les années pour lesquelles on a constaté une rupture
         for (int i = 0; i < this._serieYear.length; i++) {
            _topNStatistics.add(
                  emptyTopN(this._serieYear[i])
             );
         }

      } catch (Exception e) {
         logger.error("populateTopN : {}",e.getMessage());
      } finally {
      } 
      
      return _topNStatistics;      
   }
   
   @SuppressWarnings("hiding")
   protected abstract <T> T readTopN(List<T> _stats, int _year);

   @SuppressWarnings("hiding")
   protected abstract <T> T emptyTopN(int _year);

   /**
    * Retourne le classement pour une race et une année donnée
    * 
    * @param _list   Données de production générique pour une liste d'affixe/d'étalon
    * @return
    */
   @SuppressWarnings("hiding")
   protected <T> List<T> populateTopOfTheYear(List<? extends GenericStatistics> _list) {

      int _year = 0;
      List<T> _topOfTheYearStatistics = new ArrayList<T>();

      try {
         // Lecture de la dernière date de calcul pour définir la période (rupture dans les années)
         // A voir si les années précédentes ne feront pas l'objet d'une suppression côté
         // data (BdD); auquel cas, ce code sera obsolète
         setYearSeries(this._idBreed);
   
         // Lecture des variétés référencées
         // si une variété n'est pas représentée pour l'année, il faut l'ajouter avec qtity = 0
         setVarietiesByIdBreed(this._idBreed);
         
         Map<Integer, List<T>> _breedGroupByYear = getYearStatistics(_list);
         for (Map.Entry<Integer, List<T>> _breedOverYear : _breedGroupByYear.entrySet()) {
            
            _year = _breedOverYear.getKey();
   
            // Suppression de l'année traitée
            this._serieYear = ArrayUtils.removeElement(this._serieYear, _year);
            
            _topOfTheYearStatistics.add(
                  readTopOfTheYear(_breedOverYear.getValue(), _year)
            );
            
         }
         
         // On finalise en initialisant les années pour lesquelles on a constaté une rupture
         for (int i = 0; i < this._serieYear.length; i++) {
            _topOfTheYearStatistics.add(
                  emptyTopOfTheYear(this._serieYear[i])
             );
         }

      } catch (Exception e) {
         logger.error("populateTopOfTheYear : {}",e.getMessage());
      } finally {
      } 
      
      return _topOfTheYearStatistics;      
   }
   
   
   @SuppressWarnings("hiding")
   protected abstract <T> T readTopOfTheYear(List<T> _stats, int _year);

   @SuppressWarnings("hiding")
   protected abstract <T> T emptyTopOfTheYear(int _year);

   /**
    * Retourne les données statistiques pour l'ensemble des races du club
    *
    * @param idClub  Identifiant du club
    * @return
    */
   @SuppressWarnings("hiding")
   protected <T> List<T> populateBreeds(int idClub) {
      
      List<T> _breeds = new ArrayList<T>();
      
      try { 
         // Lecture des races associées au club pour lesquelles des données ont été calculées
         Map<TupleBreed, List<T>> _allBreeds = getDataStatistics(idClub);
         for (Map.Entry<TupleBreed, List<T>> _currentBreed : _allBreeds.entrySet()) {
   
            this._idBreed = _currentBreed.getKey().getId();
            this._nameBreed = _currentBreed.getKey().getName();
   
            // Ajout à la liste
            _breeds.add(
                  readBreed(_currentBreed.getValue())
             );
   
         }

      } catch (Exception e) {
         logger.error("populateBreeds : {}",e.getMessage());
      } finally {
      } 
      
      return _breeds;
   }

   @SuppressWarnings("hiding")
   protected abstract <T> T readBreed(List<T> _stats);

   protected void populateHeightSeries(List<? extends GenericStatistics> _stats) {
      
      IntSummaryStatistics _summaryStats = null;
      this._serieHeight = new ArrayList<SerieHeight>();
      
      try {
         @SuppressWarnings("unchecked")
         List<ConfirmationStatistics> _list = (List<ConfirmationStatistics>) _stats;
         
         // Pour la race, on prendra le min/max s/ l'ensemble des variétés
         // Pour les variétés
         for (TupleVariety _variety : this._referencedVarieties) {
            int id = _variety.getId();
            _summaryStats = _list.stream()
                  .filter(
                        ( x -> "O".equals(x.getOnTailleObligatoire()) 
                        && ( x.getTaille()!= null && x.getTaille()>0) 
                        && ( x.getIdVariete() == id ) 
                   ))
                  .collect(Collectors.summarizingInt(ConfirmationStatistics::getTaille))
             ;      
             
            int[] _serieHeight = IntStream.rangeClosed(_summaryStats.getMin(), _summaryStats.getMax()).toArray();
            for (int i = 0; i < _serieHeight.length; i++) {
               // Taille min/max s/ la variété toutes années confondues
               this._serieHeight.add( new SerieHeight(_variety.getId(), _serieHeight[i]) );
            }               
               
         }
         
      } catch (Exception e) {
         logger.error("populateSeries : {}",e.getMessage());
      } finally {
      } 
      
   }
   
}
