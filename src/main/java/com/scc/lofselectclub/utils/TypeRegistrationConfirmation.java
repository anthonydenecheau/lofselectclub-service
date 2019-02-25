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

   DESCENDANCE("LA DESCENDANCE", new ArrayList<Integer>(Arrays.asList(537,541)))
      , IMPORT("L'IMPORTATION",new ArrayList<Integer>(Arrays.asList(538)))
      , TI("TITRE INITIAL",new ArrayList<Integer>(Arrays.asList(539)))
      , LA("LIVRE D'ATTENTE",new ArrayList<Integer>(Arrays.asList(761)))
      , AUTRES("AUTRES",new ArrayList<Integer>(Arrays.asList(-1)));
   
   private final String label;
   private final List<Integer> values;
   
   TypeRegistrationConfirmation(String label, List<Integer> values) {
      this.label = label;
      this.values = values;
   }

   public String getLabel() {
      return label;
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
