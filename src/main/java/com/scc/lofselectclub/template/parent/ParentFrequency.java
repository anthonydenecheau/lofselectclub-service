package com.scc.lofselectclub.template.parent;

import java.util.Map;

import com.scc.lofselectclub.utils.TypeGender;

import io.swagger.annotations.ApiModelProperty;

public class ParentFrequency {

   @ApiModelProperty(notes = "number of parents used for the first time", position = 1, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage number of parents used for the first time over total confirmations (on the same period)", position = 3, allowEmptyValue = true)
   String percentage;

   @ApiModelProperty(dataType="Map[String, ParentFrequencyDetail]", allowableValues = "FATHER, MOTHER",notes = "detail by gender", position = 4, allowEmptyValue = true)
   Map<TypeGender, ParentFrequencyDetail> details;
   
  
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
   
   public Map<TypeGender, ParentFrequencyDetail> getDetails() {
      return details;
   }

   public void setDetails(Map<TypeGender, ParentFrequencyDetail> details) {
      this.details = details;
   }

   public ParentFrequency withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ParentFrequency withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }

   public ParentFrequency withDetails(Map<TypeGender, ParentFrequencyDetail> details) {
      this.setDetails(details);
      return this;
   }

}
