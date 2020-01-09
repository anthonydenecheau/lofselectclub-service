package com.scc.lofselectclub.template.registration;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class RegistrationBreedStatistics {

   @ApiModelProperty(notes = "year", position = 1, allowEmptyValue = true)
   int year;

   @ApiModelProperty(notes = "number of male", position = 2, allowEmptyValue = true)
   int numberOfMale;

   @ApiModelProperty(notes = "number of female", position = 3, allowEmptyValue = true)
   int numberOfFemale;

   @ApiModelProperty(notes = "number of puppies", position = 4, allowEmptyValue = true)
   int numberOfPuppies;

   @ApiModelProperty(notes = "total of litters", position = 5, allowEmptyValue = true)
   int totalOfLitter;

   @ApiModelProperty(notes = "detail by variety", position = 6, allowEmptyValue = true)
   List<RegistrationVariety> variety;

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public int getNumberOfMale() {
      return numberOfMale;
   }

   public void setNumberOfMale(int numberOfMale) {
      this.numberOfMale = numberOfMale;
   }

   public int getNumberOfFemale() {
      return numberOfFemale;
   }

   public void setNumberOfFemale(int numberOfFemale) {
      this.numberOfFemale = numberOfFemale;
   }

   public int getNumberOfPuppies() {
      return numberOfPuppies;
   }

   public void setNumberOfPuppies(int numberOfPuppies) {
      this.numberOfPuppies = numberOfPuppies;
   }

   public int getTotalOfLitter() {
      return totalOfLitter;
   }

   public void setTotalOfLitter(int totalOfLitter) {
      this.totalOfLitter = totalOfLitter;
   }

   public List<RegistrationVariety> getVariety() {
      return variety;
   }

   public void setVariety(List<RegistrationVariety> variety) {
      this.variety = variety;
   }

   public RegistrationBreedStatistics withYear(int year) {
      this.setYear(year);
      return this;
   }

   public RegistrationBreedStatistics withNumberOfMale(int numberOfMale) {
      this.setNumberOfMale(numberOfMale);
      return this;
   }

   public RegistrationBreedStatistics withNumberOfFemale(int numberOfFemale) {
      this.setNumberOfFemale(numberOfFemale);
      return this;
   }

   public RegistrationBreedStatistics withNumberOfPuppies(int numberOfPuppies) {
      this.setNumberOfPuppies(numberOfPuppies);
      return this;
   }

   public RegistrationBreedStatistics withTotalOfLitter(int totalOfLitter) {
      this.setTotalOfLitter(totalOfLitter);
      return this;
   }

   public RegistrationBreedStatistics withVariety(List<RegistrationVariety> variety) {
      this.setVariety(variety);
      return this;
   }

}
