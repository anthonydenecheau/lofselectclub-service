package com.scc.lofselectclub.template.parent;

import java.util.Map;

import com.scc.lofselectclub.utils.TypeGender;

import io.swagger.annotations.ApiModelProperty;

public class ParentVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "origin by gender", position = 3, allowEmptyValue = true)
   Map<TypeGender, ParentGender> origins;

   @ApiModelProperty(notes = "frequency of first use", position = 4, allowEmptyValue = true)
   ParentFrequency firstUse;

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

   public Map<TypeGender, ParentGender> getOrigins() {
      return origins;
   }

   public void setOrigins(Map<TypeGender, ParentGender> origins) {
      this.origins = origins;
   }

   public ParentFrequency getFirstUse() {
      return firstUse;
   }

   public void setFirstUse(ParentFrequency firstUse) {
      this.firstUse = firstUse;
   }

   public ParentVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public ParentVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public ParentVariety withOrigins(Map<TypeGender, ParentGender> origins) {
      this.setOrigins(origins);
      return this;
   }

   public ParentVariety withFirstUse(ParentFrequency firstUse) {
      this.setFirstUse(firstUse);
      return this;
   }

}
