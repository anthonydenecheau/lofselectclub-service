package com.scc.lofselectclub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceConfig {

   @Value("${lofselectclubservice.breeder.limitTopNAffix}")
   private int limitTopNAffix;

   public int getLimitTopNAffix() {
      return limitTopNAffix;
   }

   @Value("${lofselectclubservice.parent.limitTopNFathers}")
   private int limitTopNFathers;

   public int getLimitTopNFathers() {
      return limitTopNFathers;
   }

}
