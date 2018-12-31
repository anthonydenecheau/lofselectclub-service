package com.scc.lofselectclub.template.parent;


import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

public class ParentFather {

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + id;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ParentFather other = (ParentFather) obj;
      if (id != other.id)
         return false;
      return true;
   }

   public ParentFather() {
      super();
   }

   public ParentFather(int id) {
      super();
      this.id = id;
   }

   public ParentFather(int id, String name) {
      super();
      this.id = id;
      this.name = name;
   }
   
   public ParentFather(int id, String name, int year) {
      super();
      this.id = id;
      this.name = name;
      this.year = year;
   }
   
   public ParentFather(int id, String name, int qtity, String percentage, int position) {
      super();
      this.id = id;
      this.name = name;
      this.qtity = qtity;
      this.percentage = percentage;
      this.position = position;
   }

   @ApiModelProperty(notes = "position", position = 1, allowEmptyValue = true)
   int position;
   
   @ApiModelProperty(notes = "Dog id", position = 2, allowEmptyValue = true)
   private int id;

   @ApiModelProperty(notes = "Dog name", position = 3, allowEmptyValue = true)
   private String name;

   @ApiModelProperty(notes = "number of mating", position = 4, allowEmptyValue = true)
   private int qtity;
   
   @ApiModelProperty(notes = "part of the genitor in the total litters", position = 5, allowEmptyValue = true)
   private String percentage;

   @JsonIgnore
   private int year;
   
   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
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

   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }

   public String getPercentage() {
      return percentage;
   }

   public void setPercentage(String percentage) {
      this.percentage = percentage;
   }

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }
   
   public ParentFather withPosition(int position) {
      this.setPosition(position);
      return this;
   }
   
   public ParentFather withId(int id) {
      this.setId(id);
      return this;
   }
   
   public ParentFather withName(String name) {
      this.setName(name);
      return this;
   }

   public ParentFather withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ParentFather withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }
   
}
