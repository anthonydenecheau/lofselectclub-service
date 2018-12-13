package com.scc.lofselectclub.template.confirmation;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "number of confirmation", position = 3, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "detail by Height", position = 3, allowEmptyValue = true)
   ConfirmationHeight height;

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

   public ConfirmationHeight getHeight() {
      return height;
   }

   public void setHeight(ConfirmationHeight height) {
      this.height = height;
   }

   public ConfirmationVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public ConfirmationVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public ConfirmationVariety withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ConfirmationVariety withHeight(ConfirmationHeight height) {
      this.setHeight(height);
      return this;
   }

   @Override
   public String toString() {
      return "Variety [id=" + id + ", name=" + name + ", qtity=" + qtity + ", height=" + height + "]";
   }

}
