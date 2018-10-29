package com.scc.lofselectclub.template.confirmation;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationResponseObject {

   @ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
   int size;

   @ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue = true)
   List<ConfirmationBreed> breeds;

   public int getSize() {
      return size;
   }

   public void setSize(int size) {
      this.size = size;
   }

   public List<ConfirmationBreed> getBreeds() {
      return breeds;
   }

   public void setBreeds(List<ConfirmationBreed> breeds) {
      this.breeds = breeds;
   }

   public ConfirmationResponseObject withSize(int size) {
      this.setSize(size);
      return this;
   }

   public ConfirmationResponseObject withBreeds(List<ConfirmationBreed> breeds) {
      this.setBreeds(breeds);
      return this;
   }

   public ConfirmationResponseObject() {
      super();
   }

   public ConfirmationResponseObject(int size, List<ConfirmationBreed> breeds) {
      super();
      this.size = size;
      this.breeds = breeds;
   }

   @Override
   public String toString() {
      return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
   }

}
