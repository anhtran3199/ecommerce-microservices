package com.ecommerce.gateway.config;

import com.ecommerce.gateway.security.RBACFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, RBACFilter rbacFilter) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r.path("/auth/**")
                        .uri("http://user-service:8081"))
                .route("user-service-api", r -> r.path("/api/users/**", "/api/roles/**", "/api/permissions/**")
                        .filters(f -> f.filter(rbacFilter.apply(new RBACFilter.Config())))
                        .uri("http://user-service:8081"))

                // Product Service Routes
                .route("product-service", r -> r.path("/api/products/**")
                        .filters(f -> f.filter(rbacFilter.apply(new RBACFilter.Config())))
                        .uri("http://product-service:8082"))

                // Order Service Routes
                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.filter(rbacFilter.apply(new RBACFilter.Config())))
                        .uri("http://order-service:8083"))

                // Payment Service Routes
                .route("payment-service", r -> r.path("/api/payments/**")
                        .filters(f -> f.filter(rbacFilter.apply(new RBACFilter.Config())))
                        .uri("http://payment-service:8084"))

                .build();
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}