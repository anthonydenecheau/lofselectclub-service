package com.scc.lofselectclub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceConfig {

   @Value("${breeder.limitTopNAffix}")
   private int limitTopNAffix;

   public int getLimitTopNAffix() {
      return limitTopNAffix;
   }

   @Value("${parent.limitTopNFathers}")
   private int limitTopNFathers;

   public int getLimitTopNFathers() {
      return limitTopNFathers;
   }

}
