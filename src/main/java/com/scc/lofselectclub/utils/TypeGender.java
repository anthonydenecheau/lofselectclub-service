package com.scc.lofselectclub.utils;

/**
 * Classification des résultats par type geniteur
 *
 * <li>{@link #MOTHER}</li>
 * <li>{@link #FATHER}</li>
 */
public enum TypeGender {

   MOTHER("F"), FATHER("M");

   private String value;

   public String getValue() {
      return value;
   }

   private TypeGender(String value) {
      this.value = value;
   }

   public static TypeGender fromId(String value) {
      for (TypeGender type : values()) {
         if (type.getValue().equals(value)) {
            return type;
         }
      }
      return null;
   }

}
