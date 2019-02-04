package com.scc.lofselectclub.utils;


/**
 * Classification des performances par type
 *
 * <li>{@link #TEST_COMPORTEMENT}</li>
 * <li>{@link #TITRE_EXPOSITION}</li>
 * <li>{@link #TITRE_UTILISATION}</li>
 */
public enum TypePerformance {

   TEST_COMPORTEMENT(1), TITRE_EXPOSITION(2), TITRE_UTILISATION(3);

   private int value;

   public int getValue() {
      return value;
   }

   private TypePerformance(int value) {
      this.value = value;
   }

   public static TypePerformance fromId(int id) {
      for (TypePerformance type : values()) {
         if (type.getValue() == id) {
            return type;
         }
      }
      return null;
   }

}
