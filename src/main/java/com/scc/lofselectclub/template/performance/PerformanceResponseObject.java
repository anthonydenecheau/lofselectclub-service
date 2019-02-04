package com.scc.lofselectclub.template.performance;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceResponseObject {

   @ApiModelProperty(notes = "The Total of breeds", position = 1, required = true)
   int size;

   @ApiModelProperty(notes = "The list of breeds", position = 2, required = true, allowEmptyValue = true)
   List<PerformanceBreed> breeds;

   public int getSize() {
      return size;
   }

   public void setSize(int size) {
      this.size = size;
   }

   public List<PerformanceBreed> getBreeds() {
      return breeds;
   }

   public void setBreeds(List<PerformanceBreed> breeds) {
      this.breeds = breeds;
   }

   public PerformanceResponseObject withSize(int size) {
      this.setSize(size);
      return this;
   }

   public PerformanceResponseObject withBreeds(List<PerformanceBreed> breeds) {
      this.setBreeds(breeds);
      return this;
   }

   public PerformanceResponseObject() {
      super();
   }

   public PerformanceResponseObject(int size, List<PerformanceBreed> breeds) {
      super();
      this.size = size;
      this.breeds = breeds;
   }

   @Override
   public String toString() {
      return "ResponseObject [size=" + size + ", breeds=" + breeds + "]";
   }

}
