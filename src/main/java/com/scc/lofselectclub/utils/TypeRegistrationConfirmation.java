package com.scc.lofselectclub.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classification des inscriptions par type
 *
 * <li>{@link #DESCENDANCE}</li>
 * <li>{@link #IMPORT}</li>
 * <li>{@link #TI}</li>
 * <li>{@link #LA}</li>
 * <li>{@link #AUTRES}</li>
 */
public enum TypeRegistrationConfirmation {

   DESCENDANCE(537,541), IMPORT(538), TI(539), LA(761), AUTRES(-1);

   private final List<Integer> values;
   
   TypeRegistrationConfirmation(Integer ...values) {
       this.values = Arrays.asList(values);
   }

   public List<Integer> getValues() {
       return values;
   }
   
   public static TypeRegistrationConfirmation fromId(int id) {
      for (TypeRegistrationConfirmation register : TypeRegistrationConfirmation.values()) {
          if (register.getValues().contains(id)) {
              return register;
          }
      }
      return AUTRES;
  }

   public static List<Integer> getAllValues() {
      List<Integer> values = new ArrayList<Integer>();
      for (TypeRegistrationConfirmation register : TypeRegistrationConfirmation.values()) {
         for (int i : register.getValues())
            values.add(i);
     }
      return values;
   }
   
}
