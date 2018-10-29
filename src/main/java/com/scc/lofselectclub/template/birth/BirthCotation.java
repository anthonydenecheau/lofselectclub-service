package com.scc.lofselectclub.template.birth;

import io.swagger.annotations.ApiModelProperty;

public class BirthCotation {

   @ApiModelProperty(notes = "grading (cotation)", position = 1, allowEmptyValue = true)
   int grade;

   @ApiModelProperty(notes = "qtity", position = 2, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "percentage", position = 3, allowEmptyValue = true)
   String percentage;

   public int getGrade() {
      return grade;
   }

   public void setGrade(int grade) {
      this.grade = grade;
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

   public BirthCotation withGrade(int grade) {
      this.setGrade(grade);
      return this;
   }

   public BirthCotation withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public BirthCotation withPercentage(String percentage) {
      this.setPercentage(percentage);
      return this;
   }

}
