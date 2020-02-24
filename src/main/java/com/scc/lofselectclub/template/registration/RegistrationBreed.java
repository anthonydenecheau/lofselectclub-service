package com.scc.lofselectclub.template.registration;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class RegistrationBreed {

   @ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue = true)
   List<RegistrationBreedStatistics> statistics;

   String test;

   public String getTest() {
      return test;
   }

   public void setTest(String test) {
      this.test = test;
   }

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

   public List<RegistrationBreedStatistics> getStatistics() {
      return statistics;
   }

   public void setStatistics(List<RegistrationBreedStatistics> statistics) {
      this.statistics = statistics;
   }

   public RegistrationBreed withId(int id) {
      this.setId(id);
      return this;
   }

   public RegistrationBreed withName(String name) {
      this.setName(name);
      return this;
   }

   public RegistrationBreed withStatistics(List<RegistrationBreedStatistics> statistics) {
      this.setStatistics(statistics);
      return this;
   }

   @Override
   public String toString() {
      return "Breed [id=" + id + ", name=" + name + ", statistics=" + statistics + "]";
   }

}
