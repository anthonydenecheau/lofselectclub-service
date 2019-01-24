package com.scc.lofselectclub.template.health;

import io.swagger.annotations.ApiModelProperty;

public class HealthVarietyStatistics {

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage", position = 4, allowEmptyValue = true)
   String percentage;

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

   public HealthVarietyStatistics withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public HealthVarietyStatistics withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }
}
