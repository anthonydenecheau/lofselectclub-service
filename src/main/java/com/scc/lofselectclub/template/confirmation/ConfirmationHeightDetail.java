package com.scc.lofselectclub.template.confirmation;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationHeightDetail {

   @ApiModelProperty(notes = "height", position = 1, allowEmptyValue = true)
   int height;

   @ApiModelProperty(notes = "qtity", position = 2, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage", position = 3, allowEmptyValue = true)
   String percentage;

   public int getHeight() {
      return height;
   }

   public void setHeight(int height) {
      this.height = height;
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

   public ConfirmationHeightDetail withHeight(int height) {
      this.setHeight(height);
      return this;
   }

   public ConfirmationHeightDetail withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public ConfirmationHeightDetail withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }

}
