package com.scc.lofselectclub.template.breeder;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class BreederAffixStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "top N affixe", position = 2, allowEmptyValue = true)
   List<BreederAffixRank> affixes;

   @ApiModelProperty(notes = "detail by variety", position = 3, allowEmptyValue = true)
   List<BreederAffixVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public List<BreederAffixRank> getAffixes() {
      return affixes;
   }
   
   public void setAffixes(List<BreederAffixRank> affixes) {
      this.affixes = affixes;
   }

   public List<BreederAffixVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<BreederAffixVariety> variety) {
      this.variety = variety;
   }

   public BreederAffixStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public BreederAffixStatistics withAffixes(List<BreederAffixRank> affixes) {
      this.setAffixes(affixes);
      return this;
   }
   
   public BreederAffixStatistics withVariety(List<BreederAffixVariety> variety) {
      this.setVariety(variety);
      return this;
   }

   @Override
   public String toString() {
      return "AffixStatistics [year=" + year + ", affixes=" + affixes + ", variety=" + variety + "]";
   }
}
