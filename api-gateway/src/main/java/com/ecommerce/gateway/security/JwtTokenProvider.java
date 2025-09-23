package com.ecommerce.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret:myVerySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256Bits}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        // Use HS384 to match user-service
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("username", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        }
        return false;
    }
}