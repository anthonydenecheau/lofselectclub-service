package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "quantity", position = 2, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "health tests", position = 3, allowEmptyValue = true)
   List<HealthTest> healthTest;

   @ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue = true)
   List<HealthVariety> variety;
   
   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }
   
   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }

   public List<HealthTest> getHealthTest() {
      return healthTest;
   }

   public void setHealthTest(List<HealthTest> healthTest) {
      this.healthTest = healthTest;
   }
   
   public List<HealthVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<HealthVariety> variety) {
      this.variety = variety;
   }

   public HealthBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }
   
   public HealthBreedStatistics withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public HealthBreedStatistics withHealthTest(List<HealthTest> healthTest) {
      this.setHealthTest(healthTest);
      return this;
   }
   
   public HealthBreedStatistics withHealthVariety(List<HealthVariety> variety) {
      this.setVariety(variety);
      return this;
   }
}
