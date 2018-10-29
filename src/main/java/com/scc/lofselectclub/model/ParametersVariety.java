package com.scc.lofselectclub.model;

import java.util.List;

public class ParametersVariety {

   List<SerieDefinition> series;
   int total;
   boolean topN = false;
   int year;
   
   public ParametersVariety(List<SerieDefinition> series) {
      super();
      this.series = series;
   }
   public ParametersVariety(int total) {
      super();
      this.total = total;
   }
   public ParametersVariety(int year, boolean topN) {
      super();
      this.year = year;
      this.topN = topN;
   }
   public ParametersVariety() {
   }

   public List<SerieDefinition> getSeries() {
      return series;
   }
   public void setSeries(List<SerieDefinition> series) {
      this.series = series;
   }
   public int getTotal() {
      return total;
   }
   public void setTotal(int total) {
      this.total = total;
   }
   public boolean isTopN() {
      return topN;
   }
   public void setTopN(boolean topN) {
      this.topN = topN;
   }   
   public int getYear() {
      return year;
   }
   public void setYear(int year) {
      this.year = year;
   }
}
