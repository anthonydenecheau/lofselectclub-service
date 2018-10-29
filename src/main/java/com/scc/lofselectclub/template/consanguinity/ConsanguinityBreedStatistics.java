package com.scc.lofselectclub.template.consanguinity;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguinityBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "coefficient of consanguinity", position = 2, allowEmptyValue = true)
   String cng;

   @ApiModelProperty(notes = "number of litter by common ancestors", position = 3, allowEmptyValue = true)
   List<ConsanguintyCommonAncestor> litterByCommonAncestor;

   @ApiModelProperty(notes = "detail by variety", position = 6, allowEmptyValue = true)
   List<ConsanguinityVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public String getCng() {
      return cng;
   }

   public void setCng(String cng) {
      this.cng = cng;
   }

   public List<ConsanguintyCommonAncestor> getLitterByCommonAncestor() {
      return litterByCommonAncestor;
   }

   public void setLitterByCommonAncestor(List<ConsanguintyCommonAncestor> litterByCommonAncestor) {
      this.litterByCommonAncestor = litterByCommonAncestor;
   }

   public List<ConsanguinityVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<ConsanguinityVariety> variety) {
      this.variety = variety;
   }

   public ConsanguinityBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public ConsanguinityBreedStatistics withCng(String cng) {
      this.setCng(cng);
      return this;
   }

   public ConsanguinityBreedStatistics withLitterByCommonAncestor(
         List<ConsanguintyCommonAncestor> litterByCommonAncestor) {
      this.setLitterByCommonAncestor(litterByCommonAncestor);
      return this;
   }

   public ConsanguinityBreedStatistics withVariety(List<ConsanguinityVariety> variety) {
      this.setVariety(variety);
      return this;
   }

}
