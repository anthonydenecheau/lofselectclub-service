package com.scc.lofselectclub.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "authentification")
public class AuthenticateConfig {

	@Value("${authentification.value}")
	private String value;

	private List<String> keys = new ArrayList<String>();

	public List<String> getKeys() {
		return this.keys;
	}

	public String getValue() {
		return this.value;
	}

}