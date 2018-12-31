package com.scc.lofselectclub.template.health;

import java.util.List;

import com.scc.lofselectclub.utils.TypeHealth;

import io.swagger.annotations.ApiModelProperty;

public class HealthType {

   @ApiModelProperty(notes = "type", dataType = "com.scc.lofselectclub.utils.TypeHealth", position = 1, allowEmptyValue = true)
   TypeHealth type;

   @ApiModelProperty(notes = "quantity", position = 2, allowEmptyValue = true)
   int qtity;
   
   @ApiModelProperty(notes = "health family", position = 3, allowEmptyValue = true)
   List<HealthFamily> healthFamily;

   public TypeHealth getType() {
      return type;
   }

   public void setType(TypeHealth type) {
      this.type = type;
   }

   public int getQtity() {
      return qtity;
   }

   public void setQtity(int qtity) {
      this.qtity = qtity;
   }
   
   public List<HealthFamily> getHealthFamily() {
      return healthFamily;
   }

   public void setHealthFamily(List<HealthFamily> healthFamily) {
      this.healthFamily = healthFamily;
   }

   public HealthType withType(TypeHealth type) {
      this.setType(type);
      return this;
   }
   
   public HealthType withQtity(int qtity) {
      this.setQtity(qtity);
      return this;
   }

   public HealthType withHealthFamily(List<HealthFamily> healthFamily) {
      this.setHealthFamily(healthFamily);
      return this;
   }

}
