package com.ecommerce.common.security;

import com.ecommerce.common.exception.AccessDeniedException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
@Order(1)
public class AuthorizationAspect {

    @Autowired
    private AuthorizationService authorizationService;

    @Around("@annotation(requiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        String resource = requiresPermission.resource();
        String action = requiresPermission.action();
        String message = requiresPermission.message();

        if (!authorizationService.hasPermission(resource, action)) {
            throw new AccessDeniedException(message);
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(requiresRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
        String[] requiredRoles = requiresRole.value();
        String message = requiresRole.message();

        boolean hasAnyRole = Arrays.stream(requiredRoles)
                .anyMatch(authorizationService::hasRole);

        if (!hasAnyRole) {
            throw new AccessDeniedException(message + ". Required roles: " + Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();
    }

    @Around("@within(requiresPermission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint, RequiresPermission requiresPermission) throws Throwable {
        // Check if method has its own annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (method.isAnnotationPresent(RequiresPermission.class)) {
            // Method annotation takes precedence, skip class-level check
            return joinPoint.proceed();
        }

        String resource = requiresPermission.resource();
        String action = requiresPermission.action();
        String message = requiresPermission.message();

        if (!authorizationService.hasPermission(resource, action)) {
            throw new AccessDeniedException(message);
        }

        return joinPoint.proceed();
    }

    @Around("@within(requiresRole)")
    public Object checkClassRole(ProceedingJoinPoint joinPoint, RequiresRole requiresRole) throws Throwable {
        // Check if method has its own annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        if (method.isAnnotationPresent(RequiresRole.class)) {
            // Method annotation takes precedence, skip class-level check
            return joinPoint.proceed();
        }

        String[] requiredRoles = requiresRole.value();
        String message = requiresRole.message();

        boolean hasAnyRole = Arrays.stream(requiredRoles)
                .anyMatch(authorizationService::hasRole);

        if (!hasAnyRole) {
            throw new AccessDeniedException(message + ". Required roles: " + Arrays.toString(requiredRoles));
        }

        return joinPoint.proceed();
    }
}