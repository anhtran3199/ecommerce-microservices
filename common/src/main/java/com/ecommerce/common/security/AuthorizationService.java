package com.ecommerce.common.security;

import com.ecommerce.common.util.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

@Service
public class AuthorizationService {

    @Value("${services.user-service.url:http://user-service:8081}")
    private String userServiceUrl;

    private final RestTemplate restTemplate;

    public AuthorizationService() {
        this.restTemplate = new RestTemplate();
    }

    public boolean hasPermission(String resource, String action) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            if (userId == null) {
                return false;
            }

            String url = userServiceUrl + "/api/roles/user/" + userId + "/check";
            url += "?resource=" + resource + "&action=" + action;

            String token = SecurityUtil.getCurrentUserToken();
            HttpHeaders headers = new HttpHeaders();
            if (token != null) {
                headers.setBearerAuth(token);
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Boolean> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Boolean.class);

            return response.getBody() != null && response.getBody();
        } catch (Exception e) {
            // Log error and deny access by default
            System.err.println("Error checking permission: " + e.getMessage());
            return false;
        }
    }

    public boolean hasRole(String roleName) {
        try {
            Set<String> userRoles = SecurityUtil.getCurrentUserRoles();
            return userRoles != null && userRoles.contains(roleName);
        } catch (Exception e) {
            // Log error and deny access by default
            System.err.println("Error checking role: " + e.getMessage());
            return false;
        }
    }

    public boolean hasAnyRole(String... roleNames) {
        try {
            Set<String> userRoles = SecurityUtil.getCurrentUserRoles();
            if (userRoles == null) {
                return false;
            }

            for (String roleName : roleNames) {
                if (userRoles.contains(roleName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // Log error and deny access by default
            System.err.println("Error checking roles: " + e.getMessage());
            return false;
        }
    }

    public boolean isCurrentUser(Long userId) {
        try {
            Long currentUserId = SecurityUtil.getCurrentUserId();
            return currentUserId != null && currentUserId.equals(userId);
        } catch (Exception e) {
            return false;
        }
    }
}