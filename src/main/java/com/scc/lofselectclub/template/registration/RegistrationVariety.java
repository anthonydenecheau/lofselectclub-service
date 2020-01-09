package com.scc.lofselectclub.template.registration;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class RegistrationVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "number of male", position = 3, allowEmptyValue = true)
   int numberOfMale;

   @ApiModelProperty(notes = "number of female", position = 4, allowEmptyValue = true)
   int numberOfFemale;

   @ApiModelProperty(notes = "number of puppies", position = 5, allowEmptyValue = true)
   int numberOfPuppies;

   @ApiModelProperty(notes = "total of litters", position = 6, allowEmptyValue = true)
   int totalOfLitter;

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

   public RegistrationVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public RegistrationVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public RegistrationVariety withNumberOfMale(int numberOfMale) {
      this.setNumberOfMale(numberOfMale);
      return this;
   }

   public RegistrationVariety withNumberOfFemale(int numberOfFemale) {
      this.setNumberOfFemale(numberOfFemale);
      return this;
   }

   public RegistrationVariety withNumberOfPuppies(int numberOfPuppies) {
      this.setNumberOfPuppies(numberOfPuppies);
      return this;
   }

   public RegistrationVariety withTotalOfLitter(int totalOfLitter) {
      this.setTotalOfLitter(totalOfLitter);
      return this;
   }

}
