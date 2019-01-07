package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "health results", position = 4, allowEmptyValue = true)
   List<HealthResult> healthResults;

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

   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }

   public List<HealthResult> getHealthResults() {
      return healthResults;
   }

   public void setHealthResults(List<HealthResult> healthResults) {
      this.healthResults = healthResults;
   }
   
   public HealthVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public HealthVariety withName(String name) {
      this.setName(name);
      return this;
   }
   
   public HealthVariety withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public HealthVariety withHealthResults(List<HealthResult> healthResults) {
      this.setHealthResults(healthResults);
      return this;
   }
}
