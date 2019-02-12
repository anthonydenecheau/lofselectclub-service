package com.scc.lofselectclub.template.performance;

import java.util.List;
import java.util.Map;

import com.scc.lofselectclub.utils.TypeGender;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceTypeDetail {

   @ApiModelProperty(notes = "quantity", position = 1, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "quantity by gender", position = 2, allowEmptyValue = true)
   Map<TypeGender, Integer> gender;
   
   @ApiModelProperty(notes = "performance detail", position = 3, allowEmptyValue = true)
   List<PerformanceResult> results;


   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }

   public Map<TypeGender, Integer> getGender() {
      return gender;
   }

   public void setGender(Map<TypeGender, Integer> gender) {
      this.gender = gender;
   }
   
   public List<PerformanceResult> getResults() {
      return results;
   }

   public void setResults(List<PerformanceResult> results) {
      this.results = results;
   }

   public PerformanceTypeDetail withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public PerformanceTypeDetail withGender(Map<TypeGender, Integer> gender) {
      this.setGender(gender);
      return this;
   }
   
   public PerformanceTypeDetail withResults(List<PerformanceResult> results) {
      this.setResults(results);
      return this;
   }

}
