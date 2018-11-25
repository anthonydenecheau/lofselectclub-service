package com.scc.lofselectclub.template.health;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class HealthBreed {

   @ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "Breed statistics by health family", position = 3, allowEmptyValue = true)
   List<HealthFamily> healthFamily;
   
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

   public List<HealthFamily> getHealthFamily() {
      return healthFamily;
   }

   public void setHealthFamily(List<HealthFamily> healthFamily) {
      this.healthFamily = healthFamily;
   }

   public HealthBreed withId(int id) {
      this.setId(id);
      return this;
   }

   public HealthBreed withName(String name) {
      this.setName(name);
      return this;
   }

   public HealthBreed withHealthFamily(List<HealthFamily> healthFamily) {
      this.setHealthFamily(healthFamily);
      return this;
   }

   @Override
   public String toString() {
      return "Breed [id=" + id + ", name=" + name + ", healthFamily=" + healthFamily + "]";
   }

}
