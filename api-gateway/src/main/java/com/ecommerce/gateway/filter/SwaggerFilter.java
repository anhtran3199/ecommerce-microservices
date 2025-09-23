package com.ecommerce.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SwaggerFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Remove Authorization header for swagger endpoints
        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
            return chain.filter(exchange.mutate()
                    .request(exchange.getRequest().mutate()
                            .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                            .build())
                    .build());
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute before security filters
    }
}