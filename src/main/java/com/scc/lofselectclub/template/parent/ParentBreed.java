package com.scc.lofselectclub.template.parent;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class ParentBreed {

   @ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue = true)
   List<ParentBreedStatistics> statistics;

   @ApiModelProperty(notes = "Fathers topN", position = 4, allowEmptyValue = true)
   List<ParentFatherStatistics> topN;

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

   public List<ParentBreedStatistics> getStatistics() {
      return statistics;
   }

   public void setStatistics(List<ParentBreedStatistics> statistics) {
      this.statistics = statistics;
   }

   public List<ParentFatherStatistics> getTopN() {
      return topN;
   }

   public void setTopN(List<ParentFatherStatistics> topN) {
      this.topN = topN;
   }

   public ParentBreed withId(int id) {
      this.setId(id);
      return this;
   }

   public ParentBreed withName(String name) {
      this.setName(name);
      return this;
   }

   public ParentBreed withStatistics(List<ParentBreedStatistics> statistics) {
      this.setStatistics(statistics);
      return this;
   }

   public ParentBreed withTopN(List<ParentFatherStatistics> topN) {
      this.setTopN(topN);
      return this;
   }

   @Override
   public String toString() {
      return "Breed [id=" + id + ", name=" + name + ", statistics=" + statistics + ", topN=" + topN + "]";
   }

}
