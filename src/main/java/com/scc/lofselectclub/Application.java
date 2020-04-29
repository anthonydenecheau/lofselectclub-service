package com.scc.lofselectclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

import com.scc.lofselectclub.config.AuthenticateConfig;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
//@EnableConfigurationProperties(AuthenticateConfig.class)
@RefreshScope
public class Application {

   public static void main(String[] args) {
      SpringApplication.run(Application.class, args);
   }

}
