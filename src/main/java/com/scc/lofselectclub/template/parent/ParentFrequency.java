package com.scc.lofselectclub.template.parent;

import io.swagger.annotations.ApiModelProperty;

public class ParentFrequency {

   @ApiModelProperty(notes = "n time use", position = 1, allowEmptyValue = true)
   int time;

   @ApiModelProperty(notes = "number of male", position = 2, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage", position = 3, allowEmptyValue = true)
   String percentage;

   public int getTime() {
      return time;
   }

   public void setTime(int time) {
      this.time = time;
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
   
   public ParentFrequency withTime(int time) {
      this.setTime(time);
      return this;
   }

   public ParentFrequency withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ParentFrequency withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }

}
