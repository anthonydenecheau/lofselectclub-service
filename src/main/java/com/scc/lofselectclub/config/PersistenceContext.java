package com.scc.lofselectclub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = {
        "com.scc.lofselectclub"
})
@EnableTransactionManagement
@EnableSpringDataWebSupport
public class PersistenceContext {

}
