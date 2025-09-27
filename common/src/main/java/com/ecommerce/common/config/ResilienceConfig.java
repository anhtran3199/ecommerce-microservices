package com.ecommerce.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import feign.FeignException;
import feign.RetryableException;

import java.time.Duration;

@Configuration
public class ResilienceConfig {

    @Bean
    public Customizer<Resilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .timeLimiterConfig(TimeLimiterConfig.custom()
                        .timeoutDuration(Duration.ofSeconds(4))
                        .build())
                .circuitBreakerConfig(CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .waitDurationInOpenState(Duration.ofMillis(1000))
                        .slidingWindowSize(2)
                        .minimumNumberOfCalls(2)
                        .build())
                .build());
    }

    @Bean
    public RetryConfig defaultRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofSeconds(1))
                .retryOnException(ex ->
                    ex instanceof RetryableException ||
                    ex instanceof java.net.SocketTimeoutException ||
                    ex instanceof java.net.ConnectException ||
                    ex instanceof org.springframework.web.client.ResourceAccessException
                )
                .build();
    }

    @Bean
    public RetryConfig feignRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(1000))
                .retryOnException(ex ->
                    ex instanceof RetryableException ||
                    ex instanceof FeignException.InternalServerError ||
                    ex instanceof FeignException.BadGateway ||
                    ex instanceof FeignException.ServiceUnavailable ||
                    ex instanceof FeignException.GatewayTimeout
                )
                .build();
    }
}