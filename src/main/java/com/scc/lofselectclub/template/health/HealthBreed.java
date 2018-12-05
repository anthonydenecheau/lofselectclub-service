package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthBreed {

   @ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "Breed statistics by health type", position = 3, allowEmptyValue = true)
   List<HealthType> healthType;
   
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

   public List<HealthType> getHealthType() {
      return healthType;
   }

   public void setHealthType(List<HealthType> healthType) {
      this.healthType = healthType;
   }

   public HealthBreed withId(int id) {
      this.setId(id);
      return this;
   }

   public HealthBreed withName(String name) {
      this.setName(name);
      return this;
   }

   public HealthBreed withHealthType(List<HealthType> healthType) {
      this.setHealthType(healthType);
      return this;
   }

   @Override
   public String toString() {
      return "Breed [id=" + id + ", name=" + name + ", healthType=" + healthType + "]";
   }

}
