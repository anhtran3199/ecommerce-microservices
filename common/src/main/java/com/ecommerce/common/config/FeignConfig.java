package com.ecommerce.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Retryer;
import io.github.resilience4j.feign.FeignDecorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Autowired
    private RetryConfig feignRetryConfig;

    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }

    @Bean
    public FeignDecorators feignDecorators() {
        return FeignDecorators.builder()
                .withRetry(
                    Retry.of("feign-retry", feignRetryConfig)
                )
                .build();
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // Try RequestContextHolder first
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                String authorization = null;

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    authorization = request.getHeader("Authorization");
                    System.out.println("Got Authorization from RequestContextHolder: " + authorization);
                }

                // Fallback: try to get from SecurityContext if available
                if (authorization == null) {
                    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null && auth.getCredentials() instanceof String) {
                        authorization = "Bearer " + auth.getCredentials().toString();
                        System.out.println("Got Authorization from SecurityContext: " + authorization);
                    }
                }

                if (authorization != null) {
                    requestTemplate.header("Authorization", authorization);
                    System.out.println("Added Authorization header to Feign request");
                } else {
                    System.out.println("No Authorization header available");
                }

                System.out.println("Final Feign request headers: " + requestTemplate.headers());
            }
        };
    }
}