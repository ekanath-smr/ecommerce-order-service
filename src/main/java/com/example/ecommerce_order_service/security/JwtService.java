package com.example.ecommerce_order_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("JWT Secret initialized successfully");
    }

    // =========================
    // EXTRACT USERNAME
    // =========================
    public String extractUsername(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (Exception ex) {
            log.error("Failed to extract username from token", ex);
            throw new JwtException("Invalid JWT token");
        }
    }

    // =========================
    // EXTRACT ROLES
    // =========================
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        try {
            Object roles = extractAllClaims(token).get("roles");

            if (roles instanceof List<?>) {
                return (List<String>) roles;
            }

            return Collections.emptyList();
        } catch (Exception ex) {
            log.error("Failed to extract roles from token", ex);
            return Collections.emptyList();
        }
    }

    // =========================
    // EXTRACT USER ID
    // =========================
    public Long extractUserId(String token) {
        try {
            Object uid = extractAllClaims(token).get("uid");

            if (uid == null) {
                throw new JwtException("User ID not present in token");
            }

            return Long.valueOf(uid.toString());
        } catch (Exception ex) {
            log.error("Failed to extract userId from token", ex);
            throw new JwtException("Invalid JWT token");
        }
    }

    // =========================
    // VALIDATE TOKEN
    // =========================
    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);

            boolean isValid = claims.getExpiration() != null
                    && claims.getExpiration().after(new Date());

            if (!isValid) {
                log.warn("JWT token expired");
            }

            return isValid;

        } catch (JwtException ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        } catch (Exception ex) {
            log.error("Unexpected error during JWT validation", ex);
            return false;
        }
    }

    // =========================
    // EXTRACT TOKEN TYPE
    // =========================
    public String extractTokenType(String token) {
        Object tokenType = extractAllClaims(token).get("tokenType");
        return tokenType != null ? tokenType.toString() : "";
    }

    // =========================
    // INTERNAL: EXTRACT CLAIMS
    // =========================
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}