package com.scc.lofselectclub.template.parent;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ParentResponseObject {

   @ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
   int size;

   @ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue = true)
   List<ParentBreed> breeds;

   public int getSize() {
      return size;
   }

   public void setSize(int size) {
      this.size = size;
   }

   public List<ParentBreed> getBreeds() {
      return breeds;
   }

   public void setBreeds(List<ParentBreed> breeds) {
      this.breeds = breeds;
   }

   public ParentResponseObject withSize(int size) {
      this.setSize(size);
      return this;
   }

   public ParentResponseObject withBreeds(List<ParentBreed> breeds) {
      this.setBreeds(breeds);
      return this;
   }

   public ParentResponseObject() {
      super();
   }

   public ParentResponseObject(int size, List<ParentBreed> breeds) {
      super();
      this.size = size;
      this.breeds = breeds;
   }

   @Override
   public String toString() {
      return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
   }

}
