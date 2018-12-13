package com.scc.lofselectclub.template.confirmation;

import java.util.List;

import com.scc.lofselectclub.template.confirmation.ConfirmationBreedStatistics;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "number of confirmation", position = 2, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "detail by Height (if mandatory for the breed)", position = 3, allowEmptyValue = true)
   ConfirmationHeight height;

   @ApiModelProperty(notes = "detail by variety", position = 4, allowEmptyValue = true)
   List<ConfirmationVariety> variety;

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

   public ConfirmationHeight getHeight() {
      return height;
   }

   public void setHeight(ConfirmationHeight height) {
      this.height = height;
   }

   public List<ConfirmationVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<ConfirmationVariety> variety) {
      this.variety = variety;
   }

   public ConfirmationBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public ConfirmationBreedStatistics withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ConfirmationBreedStatistics withHeight(ConfirmationHeight height) {
      this.setHeight(height);
      return this;
   }

   public ConfirmationBreedStatistics withVariety(List<ConfirmationVariety> variety) {
      this.setVariety(variety);
      return this;
   }

   @Override
   public String toString() {
      return "BreedStatistics [year=" + year + ", qtity=" + qtity + ", height=" + height + ", variety=" + variety + "]";
   }

}
