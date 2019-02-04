package com.scc.lofselectclub.template.performance;

import java.util.List;
import java.util.Map;

import com.scc.lofselectclub.utils.TypePerformance;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "performance by type", position = 2, allowEmptyValue = true)
   Map<TypePerformance, PerformanceTypeDetail> performances;

   @ApiModelProperty(notes = "detail by variety", position = 3, allowEmptyValue = true)
   List<PerformanceVariety> variety;
   
   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }
   
   public Map<TypePerformance, PerformanceTypeDetail> getPerformances() {
      return performances;
   }

   public void setPerformances(Map<TypePerformance, PerformanceTypeDetail> performances) {
      this.performances = performances;
   }
   
   public List<PerformanceVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<PerformanceVariety> variety) {
      this.variety = variety;
   }
   
   public PerformanceBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public PerformanceBreedStatistics withPerformances(Map<TypePerformance, PerformanceTypeDetail> performances) {
      this.setPerformances(performances);
      return this;
   }
   
   public PerformanceBreedStatistics withVariety(List<PerformanceVariety> variety) {
      this.setVariety(variety);
      return this;
   }
}
