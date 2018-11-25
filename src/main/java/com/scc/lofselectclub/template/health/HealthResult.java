package com.scc.lofselectclub.template.health;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

public class HealthResult {

   @ApiModelProperty(notes = "health test result code", position = 1, allowEmptyValue = true)
   String code;

   @ApiModelProperty(notes = "health test result name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage", position = 4, allowEmptyValue = true)
   String percentage;

   @ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue = true)
   List<HealthVariety> variety;

   @JsonIgnore
   private int sort;

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

   public String getPercentage() {
      return percentage;
   }

   public void setPercentage(String percentage) {
      this.percentage = percentage;
   }

   public List<HealthVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<HealthVariety> variety) {
      this.variety = variety;
   }

   
   public int getSort() {
      return sort;
   }

   public void setSort(int sort) {
      this.sort = sort;
   }
   
   public HealthResult withCode(String code) {
      this.setCode(code);
      return this;
   }

   public HealthResult withName(String name) {
      this.setName(name);
      return this;
   }

   public HealthResult withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public HealthResult withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }

   public HealthResult withVariety(List<HealthVariety> variety) {
      this.setVariety(variety);
      return this;
   }

   public HealthResult withSort(int sort) {
      this.setSort(sort);
      return this;
   }

}
