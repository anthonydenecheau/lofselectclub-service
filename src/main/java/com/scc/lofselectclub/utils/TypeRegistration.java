package com.scc.lofselectclub.utils;

/**
 * Classification des inscriptions par type
 *
 * <li>{@link #FRANCAIS}</li>
 * <li>{@link #IMPORTES}</li>
 * <li>{@link #ETRANGERS}</li>
 * <li>{@link #AUTRES}</li>
 */
public enum TypeRegistration {

   FRANCAIS(537), IMPORTES(538), ETRANGERS(540), AUTRES(-1);

   private int value;

   public int getValue() {
      return value;
   }

   private TypeRegistration(int value) {
      this.value = value;
   }

   public static TypeRegistration fromId(int id) {
      for (TypeRegistration type : values()) {
         if (type.getValue() == id) {
            return type;
         }
      }
      return AUTRES;
   }

}