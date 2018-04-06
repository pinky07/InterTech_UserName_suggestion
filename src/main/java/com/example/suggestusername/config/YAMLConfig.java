package com.example.suggestusername.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
public class YAMLConfig {

	private int minInputLength;
	private int maxResults;

	public int getMinInputLength() {
		return minInputLength;
	}

	public void setMinInputLength(int minInputLength) {
		this.minInputLength = minInputLength;
	}

	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
}
