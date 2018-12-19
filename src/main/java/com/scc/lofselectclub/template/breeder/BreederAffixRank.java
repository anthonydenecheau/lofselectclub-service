package com.scc.lofselectclub.template.breeder;

import io.swagger.annotations.ApiModelProperty;

public class BreederAffixRank {

   @ApiModelProperty(notes = "position", position = 1, allowEmptyValue = true)
   int position;

   @ApiModelProperty(notes = "name", position = 2, allowEmptyValue = true)
   private String name;

   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   private int qtity;

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
