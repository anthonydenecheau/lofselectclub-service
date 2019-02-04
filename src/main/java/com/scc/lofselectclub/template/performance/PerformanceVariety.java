package com.scc.lofselectclub.template.performance;

import java.util.Map;

import com.scc.lofselectclub.utils.TypePerformance;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "performance by type", position = 2, allowEmptyValue = true)
   Map<TypePerformance, PerformanceTypeDetail> performances;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Map<TypePerformance, PerformanceTypeDetail> getPerformances() {
      return performances;
   }

   public void setPerformances(Map<TypePerformance, PerformanceTypeDetail> performances) {
      this.performances = performances;
   }

   public PerformanceVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public PerformanceVariety withName(String name) {
      this.setName(name);
      return this;
   }
   
   public PerformanceVariety withPerformances(Map<TypePerformance, PerformanceTypeDetail> performances) {
      this.setPerformances(performances);
      return this;
   }

}
