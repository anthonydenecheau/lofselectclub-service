package com.scc.lofselectclub.template.dna;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class DnaBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "number of results ", position = 2, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "number of dna results", position = 3, allowEmptyValue = true)
   int dna;

   @ApiModelProperty(notes = "number of dnaComp results", position = 4, allowEmptyValue = true)
   int dnaComp;

   @ApiModelProperty(notes = "number of dnaCompP results", position = 5, allowEmptyValue = true)
   int dnaCompP;

   @ApiModelProperty(notes = "number of dnaCompM results", position = 6, allowEmptyValue = true)
   int dnaCompM;

   @ApiModelProperty(notes = "detail by variety", position = 7, allowEmptyValue = true)
   List<DnaVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }
   
   public int getDna() {
      return dna;
   }

   public void setDna(int dna) {
      this.dna = dna;
   }

   public int getDnaComp() {
      return dnaComp;
   }

   public void setDnaComp(int dnaComp) {
      this.dnaComp = dnaComp;
   }

   public int getDnaCompP() {
      return dnaCompP;
   }

   public void setDnaCompP(int dnaCompP) {
      this.dnaCompP = dnaCompP;
   }

   public int getDnaCompM() {
      return dnaCompM;
   }

   public void setDnaCompM(int dnaCompM) {
      this.dnaCompM = dnaCompM;
   }

   public List<DnaVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<DnaVariety> variety) {
      this.variety = variety;
   }

   public DnaBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public DnaBreedStatistics withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public DnaBreedStatistics withDna(int dna) {
      this.setDna(dna);
      return this;
   }

   public DnaBreedStatistics withDnaComp(int dnaComp) {
      this.setDnaComp(dnaComp);
      return this;
   }

   public DnaBreedStatistics withDnaCompP(int dnaCompP) {
      this.setDnaCompP(dnaCompP);
      return this;
   }

   public DnaBreedStatistics withDnaCompM(int dnaCompM) {
      this.setDnaCompM(dnaCompM);
      return this;
   }

   public DnaBreedStatistics withVariety(List<DnaVariety> variety) {
      this.setVariety(variety);
      return this;
   }

}
