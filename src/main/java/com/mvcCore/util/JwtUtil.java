package com.mvcCore.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for JWT token operations.
 * <p>
 * Provides methods for generating, validating, and parsing JWT tokens.
 * Uses HMAC-SHA algorithm for token signing and verification.
 * Supports both access tokens and refresh tokens with different expiration times.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret:your-secret-key-change-this-in-production-environment}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshTokenExpiration;
    
    /**
     * Gets the secret key for JWT signing.
     * <p>
     * Converts the secret string to a SecretKey using HMAC-SHA.
     * </p>
     *
     * @return SecretKey for JWT operations
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Generates an access token for a user.
     * <p>
     * The token includes user UUID, email, and role as claims.
     * Expires after the configured expiration time (default: 24 hours).
     * </p>
     *
     * @param uuid the user's unique identifier
     * @param email the user's email address
     * @param role the user's role (USER or ADMIN)
     * @return JWT access token string
     */
    public String generateToken(String uuid, String email, String role) {
        String token = Jwts.builder()
                .subject(uuid)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
        return token;
    }
    
    /**
     * Generates a refresh token.
     * <p>
     * Refresh tokens have a longer expiration time (default: 7 days)
     * and are used to obtain new access tokens without re-authentication.
     * Uses user UUID as subject for consistency with access tokens.
     * </p>
     *
     * @param uuid the user's unique identifier
     * @return JWT refresh token string
     */
    public String generateRefreshToken(String uuid) {
        String token = Jwts.builder()
                .subject(uuid)
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getSigningKey())
                .compact();
        return token;
    }
    
    /**
     * Extracts the UUID from a JWT token.
     *
     * @param token the JWT token
     * @return user UUID from token subject
     */
    public String extractUuid(String token) {
        return getClaims(token).getSubject();
    }
    
    /**
     * Extracts the email from a JWT token.
     *
     * @param token the JWT token
     * @return user email from token claims
     */
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }
    
    /**
     * Extracts the role from a JWT token.
     *
     * @param token the JWT token
     * @return user role from token claims
     */
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    
    /**
     * Validates a JWT token.
     * <p>
     * Verifies the token signature and expiration.
     * Throws ExpiredJwtException if token is expired.
     * </p>
     *
     * @param token the JWT token to validate
     * @return true if token is valid, false otherwise
     * @throws ExpiredJwtException if token is expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token to check
     * @return true if token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Parses and extracts claims from a JWT token.
     *
     * @param token the JWT token
     * @return Claims object containing token data
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
