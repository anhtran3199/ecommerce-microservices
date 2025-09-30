package com.ecommerce.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class RBACFilter extends AbstractGatewayFilterFactory<RBACFilter.Config> {

    @Value("${app.jwt.secret:myVerySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256Bits}")
    private String jwtSecret;

    @Autowired
    private AuthorizationService authorizationService;

    public RBACFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().toString();

            // Skip auth for public endpoints
            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            // Extract JWT token
            String token = extractToken(request);
            if (token == null) {
                return unauthorized(exchange.getResponse(), "Missing or invalid token");
            }

            try {
                // Validate and extract claims from JWT
                Claims claims = validateToken(token);
                Long userId = Long.parseLong(claims.getSubject());
                String username = claims.get("username", String.class);
                String email = claims.get("email", String.class);
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                // Check RBAC permissions
                return checkPermissions(path, method, userId, roles, token)
                        .flatMap(hasPermission -> {
                            if (!hasPermission) {
                                return forbidden(exchange.getResponse(), "Access denied: insufficient permissions");
                            }

                            // Add user context headers for downstream services
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", userId.toString())
                                    .header("X-User-Username", username)
                                    .header("X-User-Email", email)
                                    .header("X-User-Roles", String.join(",", roles != null ? roles : List.of()))
                                    .build();

                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        });

            } catch (Exception e) {
                return unauthorized(exchange.getResponse(), "Invalid token: " + e.getMessage());
            }
        };
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/") ||
               path.startsWith("/actuator/") ||
               path.equals("/") ||
               (path.startsWith("/api/products") && !path.contains("POST") && !path.contains("PUT") && !path.contains("DELETE"));
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Mono<Boolean> checkPermissions(String path, String method, Long userId, List<String> roles, String token) {
        // Extract resource and action from path and method
        ResourceAction resourceAction = extractResourceAction(path, method);

        if (resourceAction == null) {
            return Mono.just(true); // No specific permission required
        }

        Set<String> userRoles = new HashSet<>(roles != null ? roles : List.of());

        // Check if user has required role
        if (resourceAction.requiredRole != null) {
            boolean hasRole = authorizationService.hasRole(userRoles, resourceAction.requiredRole);
            if (!hasRole) {
                return Mono.just(false);
            }
        }

        // Check specific permission if required
        if (resourceAction.resource != null && resourceAction.action != null) {
            return authorizationService.hasPermission(userId, resourceAction.resource, resourceAction.action, token);
        }

        return Mono.just(true);
    }

    private ResourceAction extractResourceAction(String path, String method) {
        // User management
        if (path.startsWith("/api/users")) {
            return switch (method) {
                case "GET" -> new ResourceAction("USER", "READ", null);
                case "POST" -> new ResourceAction("USER", "CREATE", null);
                case "PUT" -> new ResourceAction("USER", "UPDATE", null);
                case "DELETE" -> new ResourceAction("USER", "DELETE", null);
                default -> null;
            };
        }

        // Product management
        if (path.startsWith("/api/products")) {
            return switch (method) {
                case "GET" -> null; // Public read access
                case "POST" -> new ResourceAction("PRODUCT", "CREATE", null);
                case "PUT" -> new ResourceAction("PRODUCT", "UPDATE", null);
                case "DELETE" -> new ResourceAction("PRODUCT", "DELETE", null);
                default -> null;
            };
        }

        // Order management
        if (path.startsWith("/api/orders")) {
            return switch (method) {
                case "GET" -> new ResourceAction("ORDER", "READ", null);
                case "POST" -> new ResourceAction("ORDER", "CREATE", null);
                case "PUT" -> new ResourceAction("ORDER", "UPDATE", null);
                case "DELETE" -> new ResourceAction("ORDER", "DELETE", null);
                default -> null;
            };
        }

        // Payment management
        if (path.startsWith("/api/payments")) {
            return switch (method) {
                case "GET" -> new ResourceAction("PAYMENT", "READ", null);
                case "POST" -> new ResourceAction("PAYMENT", "CREATE", null);
                default -> null;
            };
        }

        // RBAC management (Admin only)
        if (path.startsWith("/api/roles") || path.startsWith("/api/permissions")) {
            return new ResourceAction(null, null, "ADMIN");
        }

        return null;
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> forbidden(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\":\"Forbidden\",\"message\":\"%s\"}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    private static class ResourceAction {
        final String resource;
        final String action;
        final String requiredRole;

        ResourceAction(String resource, String action, String requiredRole) {
            this.resource = resource;
            this.action = action;
            this.requiredRole = requiredRole;
        }
    }

    public static class Config {
        // Configuration properties if needed
    }
}