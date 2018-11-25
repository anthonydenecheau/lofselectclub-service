package com.scc.lofselectclub.template.confirmation;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ConfirmationVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "number of confirmation", position = 2, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "average Height (if mandatory for the breed)", position = 3, allowEmptyValue = true)
   double avgHeight;

   @ApiModelProperty(notes = "detail by Height (if mandatory for the breed)", position = 4, allowEmptyValue = true)
   ConfirmationHeigthSeries series;

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

   public double getAvgHeight() {
      return avgHeight;
   }

   public void setAvgHeight(double avgHeight) {
      this.avgHeight = avgHeight;
   }

   public ConfirmationHeigthSeries getSeries() {
      return series;
   }

   public void setSeries(ConfirmationHeigthSeries series) {
      this.series = series;
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

   public ConfirmationVariety withAvgHeight(double avgHeight) {
      this.setAvgHeight(avgHeight);
      return this;
   }

   public ConfirmationVariety withSeries(ConfirmationHeigthSeries series) {
      this.setSeries(series);
      return this;
   }

   @Override
   public String toString() {
      return "Variety [id=" + id + ", name=" + name + ", qtity=" + qtity + ", avgHeight=" + avgHeight + ", series="
            + series + "]";
   }

}
