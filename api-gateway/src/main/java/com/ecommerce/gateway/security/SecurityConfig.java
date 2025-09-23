package com.ecommerce.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/api/products/active", "/api/products/category/**", "/api/products/search").permitAll()
                        .pathMatchers("/eureka/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        // Swagger UI endpoints
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        // Service-specific swagger endpoints
                        .pathMatchers("/user-service/v3/api-docs/**").permitAll()
                        .pathMatchers("/product-service/v3/api-docs/**").permitAll()
                        .pathMatchers("/order-service/v3/api-docs/**").permitAll()
                        .pathMatchers("/payment-service/v3/api-docs/**").permitAll()
                        .anyExchange().authenticated()
                )
                .build();
    }
}