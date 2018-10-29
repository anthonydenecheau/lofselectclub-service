package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthTest {

   @ApiModelProperty(notes = "health test code", position = 1, allowEmptyValue = true)
   String code;

   @ApiModelProperty(notes = "health test name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "health results", position = 4, allowEmptyValue = true)
   List<HealthResult> healthResults;

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
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

   public HealthTest withCode(String code) {
      this.setCode(code);
      return this;
   }

   public HealthTest withName(String name) {
      this.setName(name);
      return this;
   }

   public HealthTest withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public HealthTest withHealthResults(List<HealthResult> healthResults) {
      this.setHealthResults(healthResults);
      return this;
   }

}
