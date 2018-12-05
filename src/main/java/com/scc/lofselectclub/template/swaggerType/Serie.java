package com.scc.lofselectclub.template.swaggerType;

import io.swagger.annotations.ApiModelProperty;

public class Serie {

   @ApiModelProperty(notes = "Serie property", position = 1, allowEmptyValue = true)
   private String serie;

   @ApiModelProperty(notes = "Quantity property", position = 2, allowEmptyValue = true)
   private String qtity;

   @ApiModelProperty(notes = "percentage", position = 3, allowEmptyValue = true)
   String percentage;

   public String getSerie() {
      return serie;
   }

   public void setSerie(String serie) {
      this.serie = serie;
   }

   public String getQtity() {
      return qtity;
   }

   public void setQtity(String qtity) {
      this.qtity = qtity;
   }
   
   public String getPercentage() {
      return percentage;
   }

   public void setPercentage(String percentage) {
      this.percentage = percentage;
   }

}
