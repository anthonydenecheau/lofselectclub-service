package com.scc.lofselectclub.template.parent;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ParentAffixVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "top N fathers", position = 3, allowEmptyValue = true)
   List<ParentFather> fathers;

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

   public List<ParentFather> getFathers() {
      return fathers;
   }

   public void setFathers(List<ParentFather> fathers) {
      this.fathers = fathers;
   }

   public ParentAffixVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public ParentAffixVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public ParentAffixVariety withFathers(List<ParentFather> fathers) {
      this.setFathers(fathers);
      return this;
   }

   @Override
   public String toString() {
      return "AffixVariety [id=" + id + ", name=" + name + ", fathers=" + fathers + "]";
   }

}
