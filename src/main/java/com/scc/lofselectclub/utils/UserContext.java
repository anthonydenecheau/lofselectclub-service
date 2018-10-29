package com.scc.lofselectclub.utils;

import org.springframework.stereotype.Component;

@Component
public class UserContext {

   public static final String AUTHENTICATION_KEY = "X-SCC-authentification";

   private String authentificationKey = new String();

   public String getAuthentificationKey() {
      return authentificationKey;
   }

   public void setAuthentificationKey(String authentificationKey) {
      this.authentificationKey = authentificationKey;
   }

}