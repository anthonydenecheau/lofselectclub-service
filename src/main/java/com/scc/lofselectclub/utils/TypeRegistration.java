package com.scc.lofselectclub.utils;

import java.util.Arrays;
import java.util.List;

/**
 * Classification des inscriptions par type
 *
 * <li>{@link #FRANCAIS}</li>
 * <li>{@link #IMPORTES}</li>
 * <li>{@link #ETRANGERS}</li>
 * <li>{@link #AUTRES}</li>
 */
public enum TypeRegistration {

   FRANCAIS(537,539,761,541), IMPORTES(538), ETRANGERS(540), AUTRES(-1);

   private final List<Integer> values;
   
   TypeRegistration(Integer ...values) {
       this.values = Arrays.asList(values);
   }

   public List<Integer> getValues() {
       return values;
   }
   
   public static TypeRegistration fromId(int id) {
      for (TypeRegistration register : TypeRegistration.values()) {
          if (register.getValues().contains(id)) {
              return register;
          }
      }
      return AUTRES;
  }

}
