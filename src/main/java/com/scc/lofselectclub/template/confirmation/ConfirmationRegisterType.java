package com.scc.lofselectclub.template.confirmation;

import com.scc.lofselectclub.utils.TypeRegistrationConfirmation;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationRegisterType {

   @ApiModelProperty(notes = "registration type", dataType = "com.scc.lofselectclub.utils.TypeRegistrationConfirmation", position = 1, allowEmptyValue = true)
   TypeRegistrationConfirmation registration;

   @ApiModelProperty(notes = "number ", position = 2, allowEmptyValue = true)
   int qtity;
   
   public String getRegistration() {
      return registration.getLabel();
   }

   public void setRegistration(TypeRegistrationConfirmation registration) {
      this.registration = registration;
   }

   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }
   
   public ConfirmationRegisterType withRegistration(TypeRegistrationConfirmation registration) {
      this.setRegistration(registration);
      return this;
   }

   public ConfirmationRegisterType withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   @Override
   public String toString() {
      return "RegisterType [registration=" + registration + ", qtity=" + qtity + "]";
   }

}
