package com.scc.lofselectclub.template.dna;

import io.swagger.annotations.ApiModelProperty;

public class DnaVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "number of results ", position = 3, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "number of dna results", position = 4, allowEmptyValue = true)
   int dna;

   @ApiModelProperty(notes = "number of dnaComp results", position = 5, allowEmptyValue = true)
   int dnaComp;

   @ApiModelProperty(notes = "number of dnaCompP results", position = 6, allowEmptyValue = true)
   int dnaCompP;

   @ApiModelProperty(notes = "number of dnaCompM results", position = 7, allowEmptyValue = true)
   int dnaCompM;

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
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

   public DnaVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public DnaVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public DnaVariety withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public DnaVariety withDna(int dna) {
      this.setDna(dna);
      return this;
   }

   public DnaVariety withDnaComp(int dnaComp) {
      this.setDnaComp(dnaComp);
      return this;
   }

   public DnaVariety withDnaCompP(int dnaCompP) {
      this.setDnaCompP(dnaCompP);
      return this;
   }

   public DnaVariety withDnaCompM(int dnaCompM) {
      this.setDnaCompM(dnaCompM);
      return this;
   }

}
