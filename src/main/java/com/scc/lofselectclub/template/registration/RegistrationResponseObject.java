package com.scc.lofselectclub.template.registration;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class RegistrationResponseObject {

   @ApiModelProperty(notes = "The Total of registers", position = 1, required = true)
   int size;

   @ApiModelProperty(notes = "The list of registers", position = 2, required = true, allowEmptyValue = true)
   List<RegistrationBreed> breeds;

   public int getSize() {
      return size;
   }

   public void setSize(int size) {
      this.size = size;
   }

   public List<RegistrationBreed> getBreeds() {
      return breeds;
   }

   public void setBreeds(List<RegistrationBreed> breeds) {
      this.breeds = breeds;
   }

   public RegistrationResponseObject withSize(int size) {
      this.setSize(size);
      return this;
   }

   public RegistrationResponseObject withBreeds(List<RegistrationBreed> breeds) {
      this.setBreeds(breeds);
      return this;
   }

   public RegistrationResponseObject() {
      super();
   }

   public RegistrationResponseObject(int size, List<RegistrationBreed> breeds) {
      super();
      this.size = size;
      this.breeds = breeds;
   }

   @Override
   public String toString() {
      return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
   }

}
