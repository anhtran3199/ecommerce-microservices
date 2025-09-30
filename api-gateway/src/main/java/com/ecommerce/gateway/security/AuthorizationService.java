package com.ecommerce.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Service
public class AuthorizationService {

    @Value("${services.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    private final WebClient webClient;

    public AuthorizationService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<Boolean> hasPermission(Long userId, String resource, String action, String token) {
        String url = userServiceUrl + "/api/roles/user/" + userId + "/check";

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url)
                        .queryParam("resource", resource)
                        .queryParam("action", action)
                        .build())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    public Mono<List<String>> getUserPermissions(Long userId, String token) {
        String url = userServiceUrl + "/api/roles/user/" + userId + "/permissions";

        return webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .onErrorReturn(List.of());
    }

    public boolean hasRole(Set<String> userRoles, String roleName) {
        return userRoles != null && userRoles.contains(roleName);
    }

    public boolean hasAnyRole(Set<String> userRoles, String... roleNames) {
        if (userRoles == null) {
            return false;
        }

        for (String roleName : roleNames) {
            if (userRoles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }
}