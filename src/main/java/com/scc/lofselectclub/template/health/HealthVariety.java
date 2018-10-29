package com.scc.lofselectclub.template.health;

import io.swagger.annotations.ApiModelProperty;

public class HealthVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "variety statistics", position = 3, allowEmptyValue = true)
   HealthVarietyStatistics statistics;

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

   public HealthVarietyStatistics getStatistics() {
      return statistics;
   }

   public void setStatistics(HealthVarietyStatistics statistics) {
      this.statistics = statistics;
   }

   public HealthVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public HealthVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public HealthVariety withStatistics(HealthVarietyStatistics statistics) {
      this.setStatistics(statistics);
      return this;
   }

}
