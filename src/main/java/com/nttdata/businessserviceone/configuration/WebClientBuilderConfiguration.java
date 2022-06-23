package com.nttdata.businessserviceone.configuration;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Class WebClientBuilderConfiguration.
 */
@Configuration
public class WebClientBuilderConfiguration {

  @Bean
  @LoadBalanced
  public WebClient.Builder getWebClientBuilder() {
    return WebClient.builder();
  }

}
