package com.ecommerce.common.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class UserContextUtil {

    private UserContextUtil() {
    }

    public static Long getCurrentUserId() {
        try {
            String userIdHeader = getHeader("X-User-Id");
            return userIdHeader != null ? Long.parseLong(userIdHeader) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getCurrentUsername() {
        return getHeader("X-User-Username");
    }

    public static String getCurrentUserEmail() {
        return getHeader("X-User-Email");
    }

    public static Set<String> getCurrentUserRoles() {
        String rolesHeader = getHeader("X-User-Roles");
        if (rolesHeader == null || rolesHeader.trim().isEmpty()) {
            return Set.of();
        }

        return Arrays.stream(rolesHeader.split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toSet());
    }

    public static boolean hasRole(String roleName) {
        Set<String> roles = getCurrentUserRoles();
        return roles.contains(roleName);
    }

    public static boolean hasAnyRole(String... roleNames) {
        Set<String> userRoles = getCurrentUserRoles();
        for (String roleName : roleNames) {
            if (userRoles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    private static String getHeader(String headerName) {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                return request.getHeader(headerName);
            }
        } catch (Exception e) {
            // Log error but don't expose details
        }
        return null;
    }
}