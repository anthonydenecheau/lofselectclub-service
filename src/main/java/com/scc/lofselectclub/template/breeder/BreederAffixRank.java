package com.scc.lofselectclub.template.breeder;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

public class BreederAffixRank {

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
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
      BreederAffixRank other = (BreederAffixRank) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      return true;
   }

   public BreederAffixRank() {
      super();
   }

   public BreederAffixRank(int position, String name, int qtity) {
      super();
      this.position = position;
      this.name = name;
      this.qtity = qtity;
   }

   public BreederAffixRank(String name) {
      super();
      this.name = name;
   }
   
   public BreederAffixRank(String name, int year, int qtity) {
      super();
      this.name = name;
      this.year = year;
      this.qtity = qtity;
   }
   
   @ApiModelProperty(notes = "position", position = 1, allowEmptyValue = true)
   int position;

   @ApiModelProperty(notes = "name", position = 2, allowEmptyValue = true)
   private String name;

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   private int qtity;
   
   @JsonIgnore
   private int year;

   public int getPosition() {
      return position;
   }

   public void setPosition(int position) {
      this.position = position;
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

   public int getYear() {
      return year;
   }

   public void setYear(int year) {
      this.year = year;
   }

   public BreederAffixRank withPosition(int position) {
      this.setPosition(position);
      return this;
   }
   
   public BreederAffixRank withName(String name) {
      this.setName(name);
      return this;
   }
   
   public BreederAffixRank withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }


}
