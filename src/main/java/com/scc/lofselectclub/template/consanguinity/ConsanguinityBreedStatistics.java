package com.scc.lofselectclub.template.consanguinity;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguinityBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "coefficient of consanguinity", position = 2, allowEmptyValue = true)
   String cng;

   @ApiModelProperty(dataType = "com.scc.lofselectclub.template.swaggerType.Serie", notes = "detail by series", position = 3, allowEmptyValue = true)
   List<Map<String, Object>> series;
   
   @ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue = true)
   List<ConsanguinityVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public String getCng() {
      return cng;
   }

   public void setCng(String cng) {
      this.cng = cng;
   }

   public List<Map<String, Object>> getSeries() {
      return series;
   }

   public void setSeries(List<Map<String, Object>> series) {
      this.series = series;
   }

   public List<ConsanguinityVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<ConsanguinityVariety> variety) {
      this.variety = variety;
   }

   public ConsanguinityBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public ConsanguinityBreedStatistics withCng(String cng) {
      this.setCng(cng);
      return this;
   }

   public ConsanguinityBreedStatistics withSeries(List<Map<String, Object>> series) {
      this.setSeries(series);
      return this;
   }

   public ConsanguinityBreedStatistics withVariety(List<ConsanguinityVariety> variety) {
      this.setVariety(variety);
      return this;
   }

}
