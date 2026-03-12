package com.kareem.GitMatch.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Generates and validates stateless JWTs for authenticated users.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long expirationMs;

    public JwtService(
            @Value("${gitmatch.security.jwt-secret}") String secret,
            @Value("${gitmatch.security.jwt-expiration-ms:86400000}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT containing the user's ID and email.
     */
    public String generateToken(UUID userId, String email, String authProvider) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("provider", authProvider)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the user ID from a valid JWT.
     *
     * @return the user ID, or null if the token is invalid
     */
    public UUID extractUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates a JWT token.
     *
     * @return true if the token is valid and not expired
     */
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
