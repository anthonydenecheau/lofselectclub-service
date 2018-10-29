package com.scc.lofselectclub.template.breeder;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class BreederBreed {

   @ApiModelProperty(notes = "Breed id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "Breed name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "Breed statistics", position = 3, allowEmptyValue = true)
   List<BreederBreedStatistics> statistics;

   @ApiModelProperty(notes = "Affixes topN", position = 4, allowEmptyValue = true)
   List<BreederAffixStatistics> topN;

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

   public List<BreederBreedStatistics> getStatistics() {
      return statistics;
   }

   public void setStatistics(List<BreederBreedStatistics> statistics) {
      this.statistics = statistics;
   }

   public List<BreederAffixStatistics> getTopN() {
      return topN;
   }

   public void setTopN(List<BreederAffixStatistics> topN) {
      this.topN = topN;
   }

   public BreederBreed withId(int id) {
      this.setId(id);
      return this;
   }

   public BreederBreed withName(String name) {
      this.setName(name);
      return this;
   }

   public BreederBreed withStatistics(List<BreederBreedStatistics> statistics) {
      this.setStatistics(statistics);
      return this;
   }

   public BreederBreed withTopN(List<BreederAffixStatistics> topN) {
      this.setTopN(topN);
      return this;
   }

   @Override
   public String toString() {
      return "RaceObject [id=" + id + ", name=" + name + ", statistics=" + statistics + ", topN=" + topN + "]";
   }

}
