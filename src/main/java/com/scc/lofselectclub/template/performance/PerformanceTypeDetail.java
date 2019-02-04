package com.scc.lofselectclub.template.performance;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceTypeDetail {

   @ApiModelProperty(notes = "quantity", position = 1, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "performance detail", position = 2, allowEmptyValue = true)
   List<PerformanceResult> results;


   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
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
   
   public PerformanceTypeDetail withResults(List<PerformanceResult> results) {
      this.setResults(results);
      return this;
   }

}
