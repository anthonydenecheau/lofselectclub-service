package com.scc.lofselectclub.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Classification des inscriptions par type
 *
 * <li>{@link #FRANCAIS}</li>
 * <li>{@link #IMPORTES}</li>
 * <li>{@link #ETRANGERS}</li>
 */
public enum TypeRegistrationFather {

   FRANCAIS(537,539,761,541), IMPORTES(538), ETRANGERS(540);

   private final List<Integer> values;
   
   TypeRegistrationFather(Integer ...values) {
       this.values = Arrays.asList(values);
   }

   public List<Integer> getValues() {
       return values;
   }
   
   public static TypeRegistrationFather fromId(int id) {
      for (TypeRegistrationFather register : TypeRegistrationFather.values()) {
          if (register.getValues().contains(id)) {
              return register;
          }
      }
      return FRANCAIS;
  }

}
