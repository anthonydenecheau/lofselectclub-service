package com.scc.lofselectclub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthenticateConfig {

   @Value("${lofselectclubservice.authenticationKey}")
   private String key;

   @Value("${lofselectclubservice.authenticationValue}")
   private String value;

   public String getKey() {
      return this.key;
   }

   public String getValue() {
      return this.value;
   }

}