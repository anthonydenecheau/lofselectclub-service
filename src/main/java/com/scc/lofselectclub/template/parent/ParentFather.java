package com.scc.lofselectclub.template.parent;

import io.swagger.annotations.ApiModelProperty;

public class ParentFather {

   public ParentFather() {
      super();
   }

   public ParentFather(String name, int qtity) {
      super();
      this.name = name;
      this.qtity = qtity;
   }

   @ApiModelProperty(notes = "Dog name", position = 1, allowEmptyValue = true)
   private String name;

   @ApiModelProperty(notes = "number of mating", position = 2, allowEmptyValue = true)
   private int qtity;

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

   public ParentFather withName(String name) {
      this.setName(name);
      return this;
   }

   public ParentFather withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

}
