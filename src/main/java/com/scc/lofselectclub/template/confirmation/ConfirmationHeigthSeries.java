package com.scc.lofselectclub.template.confirmation;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationHeigthSeries {

   @ApiModelProperty(notes = "number of confirmation", position = 1, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "detail by Height (if mandatory for the breed)", position = 2, allowEmptyValue = true)
   Map<String, Object> series;
   
   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }

   public Map<String, Object> getSeries() {
      return series;
   }

   public void setSeries(Map<String, Object> series) {
      this.series = series;
   }
   
   public ConfirmationHeigthSeries withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public ConfirmationHeigthSeries withSeries(Map<String, Object> series) {
      this.setSeries(series);
      return this;
   }

   @Override
   public String toString() {
      return "ConfirmationHeigthSeries [qtity=" + qtity + ", series=" + series + "]";
   }
   
}
