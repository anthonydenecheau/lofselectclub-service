package com.scc.lofselectclub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lofselectclubservice")
public class AuthenticateConfig {

   @Value("${authenticationKey}")
   private String key;

   @Value("${authenticationValue}")
   private String value;

   public String getKey() {
      return this.key;
   }

   public String getValue() {
      return this.value;
   }

}