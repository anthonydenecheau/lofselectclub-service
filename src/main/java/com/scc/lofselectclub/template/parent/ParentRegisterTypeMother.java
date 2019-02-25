package com.scc.lofselectclub.template.parent;

import com.scc.lofselectclub.utils.TypeRegistrationMother;

import io.swagger.annotations.ApiModelProperty;

public class ParentRegisterTypeMother extends ParentRegisterType {

   @ApiModelProperty(notes = "registration type", dataType = "com.scc.lofselectclub.utils.TypeRegistrationMother", position = 1, allowEmptyValue = true)
   TypeRegistrationMother registration;

   @ApiModelProperty(notes = "number of male/female", position = 2, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage", position = 3, allowEmptyValue = true)
   String percentage;

   public TypeRegistrationMother getRegistration() {
      return registration;
   }

   public void setRegistration(TypeRegistrationMother registration) {
      this.registration = registration;
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

   public ParentRegisterTypeMother withRegistration(TypeRegistrationMother registration) {
      this.setRegistration(registration);
      return this;
   }

   public ParentRegisterTypeMother withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ParentRegisterTypeMother withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }

   @Override
   public String toString() {
      return "RegisterType [registration=" + registration + ", qtity=" + qtity + ", percentage=" + percentage + "]";
   }

}
