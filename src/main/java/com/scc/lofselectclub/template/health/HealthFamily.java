package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthFamily {

   @ApiModelProperty(notes = "family code", position = 1, allowEmptyValue = true)
   String code;

   @ApiModelProperty(notes = "family name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "Breed statistics", position = 4, allowEmptyValue = true)
   List<HealthBreedStatistics> statistics;
   
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
   
   public List<HealthBreedStatistics> getStatistics() {
      return statistics;
   }

   public void setStatistics(List<HealthBreedStatistics> statistics) {
      this.statistics = statistics;
   }

   public HealthFamily withCode(String code) {
      this.setCode(code);
      return this;
   }

   public HealthFamily withName(String name) {
      this.setName(name);
      return this;
   }
   
   public HealthFamily withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public HealthFamily withStatistics(List<HealthBreedStatistics> statistics) {
      this.setStatistics(statistics);
      return this;
   }

}
