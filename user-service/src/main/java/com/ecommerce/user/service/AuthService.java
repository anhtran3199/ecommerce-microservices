package com.ecommerce.user.service;

import com.ecommerce.user.dto.JwtAuthenticationResponse;
import com.ecommerce.user.dto.LoginRequest;
import com.ecommerce.user.dto.SignUpRequest;
import com.ecommerce.user.entity.Role;
import com.ecommerce.user.entity.User;
import com.ecommerce.user.repository.RoleRepository;
import com.ecommerce.user.repository.UserRepository;
import com.ecommerce.common.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    RoleRepository roleRepository;

    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles() != null ?
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()) :
                List.of();

        String jwt = tokenProvider.generateTokenFromUsername(
                user.getUsername(),
                user.getId(),
                user.getEmail(),
                roles
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public JwtAuthenticationResponse registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new RuntimeException("Email Address already in use!");
        }

        // Assign default CUSTOMER role
        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Default CUSTOMER role not found"));

        Set<Role> defaultRoles = new HashSet<>();
        defaultRoles.add(customerRole);

        User user = User.builder()
                .username(signUpRequest.getUsername())
                .email(signUpRequest.getEmail())
                .firstName(signUpRequest.getFirstName())
                .lastName(signUpRequest.getLastName())
                .phoneNumber(signUpRequest.getPhoneNumber())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .roles(defaultRoles)
                .build();

        User result = userRepository.save(user);

        List<String> roles = result.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String jwt = tokenProvider.generateTokenFromUsername(
                result.getUsername(),
                result.getId(),
                result.getEmail(),
                roles
        );

        return JwtAuthenticationResponse.builder()
                .accessToken(jwt)
                .userId(result.getId())
                .username(result.getUsername())
                .email(result.getEmail())
                .build();
    }
}