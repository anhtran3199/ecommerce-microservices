package com.ecommerce.common.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LoadBalancerConfig {

    @Bean
    @LoadBalanced
    @Lazy
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}