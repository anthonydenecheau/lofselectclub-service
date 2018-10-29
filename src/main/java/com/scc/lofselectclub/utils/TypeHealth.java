package com.scc.lofselectclub.utils;

/**
 * Classification des résultats par type
 *
 * <li>{@link #SUIVI}</li>
 * <li>{@link #SOUS_SURVEILLANCE}</li>
 * <li>{@link #EMERGENTE}</li>
 * <li>{@link #GENE_INTERET}</li>
 */
public enum TypeHealth {

   SUIVI(1), SOUS_SURVEILLANCE(2), EMERGENTE(3), GENE_INTERET(4);

   private int value;

   public int getValue() {
      return value;
   }

   private TypeHealth(int value) {
      this.value = value;
   }

   public static TypeHealth fromId(int id) {
      for (TypeHealth type : values()) {
         if (type.getValue() == id) {
            return type;
         }
      }
      return null;
   }

}
