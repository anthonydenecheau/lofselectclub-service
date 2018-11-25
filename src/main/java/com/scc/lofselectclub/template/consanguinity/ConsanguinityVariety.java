package com.scc.lofselectclub.template.consanguinity;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class ConsanguinityVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "coefficient of consanguinity", position = 3, allowEmptyValue = true)
   String cng;

   @ApiModelProperty(dataType = "com.scc.lofselectclub.template.swaggerType.Serie", notes = "detail by series", position = 4, allowEmptyValue = true)
   List<Map<String, Object>> series;

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

   public String getCng() {
      return cng;
   }

   public void setCng(String cng) {
      this.cng = cng;
   }

   public List<Map<String, Object>> getSeries() {
      return series;
   }

   public void setSeries(List<Map<String, Object>> series) {
      this.series = series;
   }

   public ConsanguinityVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public ConsanguinityVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public ConsanguinityVariety withCng(String cng) {
      this.setCng(cng);
      return this;
   }

   public ConsanguinityVariety withSeries(List<Map<String, Object>> series) {
      this.setSeries(series);
      return this;
   }

}
