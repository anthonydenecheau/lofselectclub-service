package com.scc.lofselectclub.model;


public class SerieCng {

   public SerieCng(Double minValue, Double maxValue, String libelle, int sequence) {
      super();
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.libelle = libelle;
      this.sequence = sequence;
   }

   Double minValue;
   Double maxValue;
   String libelle;
   int sequence;

   public Double getMinValue() {
      return minValue;
   }

   public void setMinValue(Double minValue) {
      this.minValue = minValue;
   }

   public Double getMaxValue() {
      return maxValue;
   }

   public void setMaxValue(Double maxValue) {
      this.maxValue = maxValue;
   }

   public String getLibelle() {
      return libelle;
   }

   public void setLibelle(String libelle) {
      this.libelle = libelle;
   }

   public Integer getSequence() {
      return sequence;
   }

   public void setSequence(Integer sequence) {
      this.sequence = sequence;
   }

}
