package com.scc.lofselectclub.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamUtils {

	final static int minusYear = 4;
	
	public static int[] findSerieYear (Date _referenceDate) {
		
		LocalDateTime _l = _referenceDate.toInstant()
			      .atZone(ZoneId.systemDefault())
			      .toLocalDateTime(); 
		return IntStream.rangeClosed(_l.minusYears(minusYear).getYear(), _l.getYear()).toArray();
	}
	
    public static <T, K extends Comparable<K>> Collector<T, ?, TreeMap<K, List<T>>> sortedGroupingBy(Function<T, K> function) {
        return Collectors.groupingBy(function, 
           TreeMap::new, Collectors.toList());
   }
    
   public static <K, V extends Comparable<V>, T extends Comparable<T>> Collection<K> topN(
            Map<K, V> map, 
            int N,
            Function<? super K, ? extends T> tieBreaker) {

        TreeMap<Map.Entry<K, V>, K> topN = new TreeMap<>(
            Map.Entry.<K, V>comparingByValue()      // by value descending, then by key
                .reversed()                         // to allow entries with duplicate values
                .thenComparing(e -> tieBreaker.apply(e.getKey())));

        map.entrySet().forEach(e -> {
          topN.put(e, e.getKey());
          if (topN.size() > N) topN.pollLastEntry();
        });

        return topN.values();
   }
   
   public static boolean breedMonoVariety (String name) {
	   return ( ("".equals(name) || name == null) ? true : false);
   }
}
