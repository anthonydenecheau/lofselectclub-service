package com.scc.lofselectclub.template.parent;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ParentFatherStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "top N fathers", position = 2, allowEmptyValue = true)
   List<ParentFather> fathers;

    @ApiModelProperty(notes = "detail by variety", position = 3, allowEmptyValue=true)
   List<ParentAffixVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public List<ParentFather> getFathers() {
      return fathers;
   }

   public void setFathers(List<ParentFather> fathers) {
      this.fathers = fathers;
   }

   public List<ParentAffixVariety> getVariety() { return variety; }
   
   public void setVariety(List<ParentAffixVariety> variety) { 
      this.variety = variety;
   }

   public ParentFatherStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public ParentFatherStatistics withFathers(List<ParentFather> fathers) {
      this.setFathers(fathers);
      return this;
   }

   public ParentFatherStatistics withVariety(List<ParentAffixVariety> variety){
      this.setVariety(variety); 
      return this; 
   }

   // @Override
   // public String toString() {
   // return "AffixStatistics [year=" + year + ", affixes=" + affixes + ",
   // variety=" + variety + "]";
   // }

}
