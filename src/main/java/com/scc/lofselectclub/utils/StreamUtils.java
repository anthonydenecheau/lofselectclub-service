package com.scc.lofselectclub.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.scc.lofselectclub.model.BreederStatistics;
import com.scc.lofselectclub.template.TupleVariety;
import com.scc.lofselectclub.template.breeder.BreederAffixRank;
import com.scc.lofselectclub.template.parent.ParentFather;

public class StreamUtils {

   final static int minusYear = 4;

   /**
    * Retourne les années de référence pour les données statistiques
    * 
    * @param _referenceDate   Année de calcul des statistiques
    * @return                 Liste des 5 dernières années
    */
   public static int[] findSerieYear(Date _referenceDate) {

      LocalDateTime _l = _referenceDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
      return IntStream.rangeClosed(_l.minusYears(minusYear).getYear(), _l.getYear()).toArray();
   }

   public static int findSerieLastYear(int[] _serieYear) {

      return Arrays.stream(_serieYear)
            .max()
            .getAsInt();
   }

   public static <T, K extends Comparable<K>> Collector<T, ?, TreeMap<K, List<T>>> sortedGroupingBy(
         Function<T, K> function) {
      return Collectors.groupingBy(function, TreeMap::new, Collectors.toList());
   }

   /**
    * Retourne les n meilleurs éléments
    * 
    * @param map  Initial map
    * @param N    Size N elements
    * @param tieBreaker
    *           Using a comparator that first sorts the TreeMap<Map.Entry<K, V>, K>
    *           by values V in descending order, and then by keys K
    * @return     Retourne les n top éléments triés par valeur décroissante
    * @See https://stackoverflow.com/questions/52343325/java-8-stream-how-to-get-top-n-count
    */
   public static <K, V extends Comparable<V>, T extends Comparable<T>> Collection<K> topN(Map<K, V> map, int N,
         Function<? super K, ? extends T> tieBreaker) {

      // First, put the entry into the topN map
      TreeMap<Map.Entry<K, V>, K> topN = new TreeMap<>(Map.Entry.<K, V>comparingByValue() // by value descending, then by key
            .reversed() // to allow entries with duplicate values
            .thenComparing(e -> tieBreaker.apply(e.getKey())));

      // Then, if the map has more than N entries,
      // we immediately invoke the pollLastEntry method will remove the entry with the lowest priority
      // (according to the order of the keys of the TreeMap)
      map.entrySet().forEach(e -> {
         topN.put(e, e.getKey());
         if (topN.size() > N)
            topN.pollLastEntry();
      });

      return topN.values();
   }

   /**
    * Stateful filter. T is type of stream element, K is type of extracted key.
    * 
    * @param key
    * @return
    */
   public static <T> Predicate<T> distinctByKey(Function<? super T, Object> key) {
      Map<Object, Boolean> map = new ConcurrentHashMap<>();
      return t -> map.putIfAbsent(key.apply(t), Boolean.TRUE) == null;
   }

   /**
    * Détermine si la variété appartient à une race mono variété
    * 
    * @param variety Données variété
    * @return        <code>true</code> si la race est mono variété
    */
   public static boolean breedMonoVariety(TupleVariety variety) {
      String name = variety.getName();
      return (("".equals(name) || name == null) ? true : false);
   }
  
   public static Predicate<ParentFather> isTopNFather(List<ParentFather> fathers) {
      return p -> fathers.contains(new ParentFather(p.getId()));  
   }

   public static Predicate<BreederStatistics> onlyTopNFather(List<ParentFather> fathers) {
      return p -> fathers.contains(new ParentFather(p.getIdEtalon(), p.getNomEtalon()));  
   }

   public static List<BreederStatistics> filterTopNFathers (List<BreederStatistics> list,
         Predicate<BreederStatistics> predicate)
   {  
         return list.stream()
                  .filter( predicate )
                  .collect(Collectors.<BreederStatistics>toList());
   }
   
   /*
   public static Predicate<BreederAffixRank> isTopNAffixe(List<BreederAffixRank> affixes) {
      return p -> affixes.contains(new BreederAffixRank(p.getName()));  
   }
   */

   public static Predicate<BreederStatistics> onlyTopNAffixe(List<BreederAffixRank> affixes) {
      return p -> affixes.contains(new BreederAffixRank(p.getAffixeEleveur()));  
   }

   public static List<BreederStatistics> filterTopNAffixes(List<BreederStatistics> list,
         Predicate<BreederStatistics> predicate)
   {  
         return list.stream()
                  .filter( predicate )
                  .collect(Collectors.<BreederStatistics>toList());
   }
}
