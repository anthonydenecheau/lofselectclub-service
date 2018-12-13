package com.scc.lofselectclub.template.confirmation;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationHeight {

   @ApiModelProperty(notes = "number of confirmation", position = 1, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "average Height", position = 2, allowEmptyValue = true)
   double avg;
   
   @ApiModelProperty(notes = "detail by Height", position = 3, allowEmptyValue = true)
   List<ConfirmationHeightDetail> details;
   
   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }

   public double getAvg() {
      return avg;
   }

   public void setAvg(double avg) {
      this.avg = avg;
   }
   
   public List<ConfirmationHeightDetail> getDetails() {
      return details;
   }

   public void setDetails(List<ConfirmationHeightDetail> details) {
      this.details = details;
   }
   
   public ConfirmationHeight withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public ConfirmationHeight withAvg(double avg) {
      this.setAvg(avg);
      return this;
   }
   
   public ConfirmationHeight withDetails(List<ConfirmationHeightDetail> details) {
      this.setDetails(details);
      return this;
   }

   @Override
   public String toString() {
      return "ConfirmationHeight [qtity=" + qtity + ", avg=" + avg + ", details=" + details + "]";
   }
   
}
