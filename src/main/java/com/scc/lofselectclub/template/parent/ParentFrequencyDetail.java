package com.scc.lofselectclub.template.parent;

import io.swagger.annotations.ApiModelProperty;

public class ParentFrequencyDetail {

   @ApiModelProperty(notes = "number by gender", position = 1, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage by gender", position = 2, allowEmptyValue = true)
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
   
   public ParentFrequencyDetail withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ParentFrequencyDetail withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }
   
}
