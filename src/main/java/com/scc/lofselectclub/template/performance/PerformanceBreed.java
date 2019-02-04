package com.scc.lofselectclub.template.performance;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceBreed {

   @ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue = true)
   List<PerformanceBreedStatistics> statistics;

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

   public List<PerformanceBreedStatistics> getStatistics() {
      return statistics;
   }

   public void setStatistics(List<PerformanceBreedStatistics> statistics) {
      this.statistics = statistics;
   }

   public PerformanceBreed withId(int id) {
      this.setId(id);
      return this;
   }

   public PerformanceBreed withName(String name) {
      this.setName(name);
      return this;
   }

   public PerformanceBreed withStatistics(List<PerformanceBreedStatistics> statistics) {
      this.setStatistics(statistics);
      return this;
   }

   @Override
   public String toString() {
      return "Breed [id=" + id + ", name=" + name + ", statistics=" + statistics + "]";
   }
   
}
