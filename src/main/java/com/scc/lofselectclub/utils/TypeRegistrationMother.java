package com.scc.lofselectclub.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Classification des inscriptions par type
 *
 * <li>{@link #FRANCAIS}</li>
 * <li>{@link #IMPORTES}</li>
 */
public enum TypeRegistrationMother {

   FRANCAIS(537,539,761,541), IMPORTES(538);

   private final List<Integer> values;
   
   TypeRegistrationMother(Integer ...values) {
       this.values = Arrays.asList(values);
   }

   public List<Integer> getValues() {
       return values;
   }
   
   public static TypeRegistrationMother fromId(int id) {
      for (TypeRegistrationMother register : TypeRegistrationMother.values()) {
          if (register.getValues().contains(id)) {
              return register;
          }
      }
      return FRANCAIS;
  }

}
