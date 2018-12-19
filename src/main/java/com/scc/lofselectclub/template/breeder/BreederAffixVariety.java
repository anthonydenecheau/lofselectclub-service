package com.scc.lofselectclub.template.breeder;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class BreederAffixVariety {

   @ApiModelProperty(notes = "variety id", position = 1, allowEmptyValue = true)
   int id;

   @ApiModelProperty(notes = "variety name", position = 2, allowEmptyValue = true)
   String name;

   @ApiModelProperty(notes = "top N affixe", position = 2, allowEmptyValue = true)
   List<BreederAffixRank> affixes;

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

   public List<BreederAffixRank> getAffixes() {
      return affixes;
   }
   
   public void setAffixes(List<BreederAffixRank> affixes) {
      this.affixes = affixes;
   }
   
   public BreederAffixVariety withId(int id) {
      this.setId(id);
      return this;
   }

   public BreederAffixVariety withName(String name) {
      this.setName(name);
      return this;
   }

   public BreederAffixVariety withAffixes(List<BreederAffixRank> affixes) {
      this.setAffixes(affixes);
      return this;
   }
   
   @Override
   public String toString() {
      return "AffixVariety [id=" + id + ", name=" + name + ", affixes=" + affixes + "]";
   }

}
