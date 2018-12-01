package com.scc.lofselectclub.template.parent;

import java.util.List;
import java.util.Map;

import com.scc.lofselectclub.template.parent.ParentVariety;
import com.scc.lofselectclub.utils.TypeGender;

import io.swagger.annotations.ApiModelProperty;

public class ParentBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "origin by gender", position = 2, allowEmptyValue = true)
   Map<TypeGender, ParentGender> origins;

   @ApiModelProperty(notes = "frequency of first use", position = 3, allowEmptyValue = true)
   ParentFrequency firstUse;

   @ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue = true)
   List<ParentVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public Map<TypeGender, ParentGender> getOrigins() {
      return origins;
   }

   public void setOrigins(Map<TypeGender, ParentGender> origins) {
      this.origins = origins;
   }

   public ParentFrequency getFirstUse() {
      return firstUse;
   }

   public void setFirstUse(ParentFrequency firstUse) {
      this.firstUse = firstUse;
   }

   public List<ParentVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<ParentVariety> variety) {
      this.variety = variety;
   }

   public ParentBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public ParentBreedStatistics withOrigins(Map<TypeGender, ParentGender> origins) {
      this.setOrigins(origins);
      return this;
   }

   public ParentBreedStatistics withVariety(List<ParentVariety> variety) {
      this.setVariety(variety);
      return this;
   }

   public ParentBreedStatistics withFirstUse(ParentFrequency firstUse) {
      this.setFirstUse(firstUse);
      return this;
   }

   @Override
   public String toString() {
      return "BreedStatistics [year=" + year + ", origins=" + origins + ", firstUse=" + firstUse + ", variety=" + variety + "]";
   }

}
