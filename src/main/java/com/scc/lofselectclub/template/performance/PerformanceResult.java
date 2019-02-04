package com.scc.lofselectclub.template.performance;

import java.util.Map;

import com.scc.lofselectclub.utils.TypeGender;

import io.swagger.annotations.ApiModelProperty;

public class PerformanceResult {

   @ApiModelProperty(notes = "Performance code", position = 1, allowEmptyValue = true)
   String code;

   @ApiModelProperty(notes = "Performance name", position = 2, allowEmptyValue = true)
   String name;
   
   @ApiModelProperty(notes = "quantity", position = 3, allowEmptyValue = true)
   int qtity;

   @ApiModelProperty(notes = "quantity by gender", position = 4, allowEmptyValue = true)
   Map<TypeGender, Integer> gender;
   

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
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
   
   public Map<TypeGender, Integer> getGender() {
      return gender;
   }

   public void setGender(Map<TypeGender, Integer> gender) {
      this.gender = gender;
   }

   public PerformanceResult withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }
   
   public PerformanceResult withCode(String code) {
      this.setCode(code);
      return this;
   }
   
   public PerformanceResult withName(String name) {
      this.setName(name);
      return this;
   }

   public PerformanceResult withGender(Map<TypeGender, Integer> gender) {
      this.setGender(gender);
      return this;
   }

}
