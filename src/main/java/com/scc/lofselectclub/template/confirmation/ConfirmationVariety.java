package com.scc.lofselectclub.template.confirmation;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "number of confirmation", position = 3, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "detail by Height", position = 4, allowEmptyValue = true)
   ConfirmationHeight height;

   @ApiModelProperty(notes = "detail by register", position = 5, allowEmptyValue = true)
   List<ConfirmationRegisterType> registerType;

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

   public List<ConfirmationRegisterType> getRegisterType() {
      return registerType;
   }

   public void setRegisterType(List<ConfirmationRegisterType> registerType) {
      this.registerType = registerType;
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

   public ConfirmationVariety withRegisterType(List<ConfirmationRegisterType> registerType) {
      this.setRegisterType(registerType);
      return this;
   }
   
   @Override
   public String toString() {
      return "Variety [id=" + id + ", name=" + name + ", qtity=" + qtity + ", height=" + height + ", registerType=" + registerType + "]";
   }

}
