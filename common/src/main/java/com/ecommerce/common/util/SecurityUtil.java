package com.ecommerce.common.util;

import com.ecommerce.common.security.JwtUserDetails;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityUtil {

	private SecurityUtil() {
	}

	public static Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof JwtUserDetails userDetails) {
			return userDetails.getId();
		}

		return null;
	}

	public static String getCurrentUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof JwtUserDetails userDetails) {
			return userDetails.getUsername();
		}

		return authentication.getName();
	}

	public static String getCurrentUserEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return null;
		}

		Object principal = authentication.getPrincipal();
		if (principal instanceof JwtUserDetails userDetails) {
			return userDetails.getEmail();
		}

		return null;
	}

	public static Set<String> getCurrentUserRoles() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return Set.of();
		}

		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring(5)) // Remove "ROLE_" prefix
				.collect(Collectors.toSet());
	}

	public static Set<String> getCurrentUserAuthorities() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return Set.of();
		}

		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
	}

	public static String getCurrentUserToken() {
		try {
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
			if (requestAttributes != null) {
				HttpServletRequest request = requestAttributes.getRequest();
				String authHeader = request.getHeader("Authorization");
				if (authHeader != null && authHeader.startsWith("Bearer ")) {
					return authHeader.substring(7);
				}
			}
		} catch (Exception e) {
			// Log error but don't expose token
		}
		return null;
	}

	public static boolean isAuthenticated() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null && authentication.isAuthenticated();
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
}
