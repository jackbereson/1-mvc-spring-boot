package com.coremvc.security;

import com.coremvc.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * JWT authentication filter following Spring Security best practices.
 * <p>
 * This filter intercepts HTTP requests, validates JWT tokens from the Authorization header,
 * and establishes Spring Security authentication context for valid tokens.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Validates JWT tokens and extracts user claims (UUID, role)</li>
 *   <li>Sets up Spring Security authentication context with user authorities</li>
 *   <li>Delegates error handling to {@link JwtExceptionHandler}</li>
 *   <li>Clears security context on invalid tokens for safety</li>
 *   <li>Path-based filtering handled by SecurityFilterChain configuration</li>
 * </ul>
 * </p>
 *
 * @author MVC Core Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final JwtExceptionHandler jwtExceptionHandler;

    /**
     * Filters incoming HTTP requests and validates JWT tokens.
     * <p>
     * Process flow:
     * <ol>
     *   <li>Extract JWT from Authorization header (if present)</li>
     *   <li>Validate token and extract user claims (UUID, role)</li>
     *   <li>Set up Spring Security authentication context with authorities</li>
     *   <li>Handle token expiration and validation errors via exception handler</li>
     *   <li>Clear security context on invalid tokens for safety</li>
     * </ol>
     * </p>
     * <p>
     * Note: Public endpoint filtering is handled by SecurityFilterChain configuration,
     * not in this filter. This follows Spring Security best practices.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Skip if no Authorization header or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip validation for refresh endpoint to allow expired access tokens
        String requestPath = request.getRequestURI();
        if (requestPath.equals("/api/v1/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Extract user information from token
            String uuid = jwtUtil.extractUuid(token);
            String role = jwtUtil.extractRole(token);

            // Only set authentication if not already authenticated
            if (uuid != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Validate token
                if (jwtUtil.validateToken(token)) {

                    // Build authorities from role
                    List<SimpleGrantedAuthority> authorities = role != null
                            ? Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            : Collections.emptyList();

                    // Create authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(uuid, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Authentication set for user: {}", uuid);
                } else {
                    log.warn("JWT token validation failed");
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT token has expired");
            SecurityContextHolder.clearContext();
            jwtExceptionHandler.handleExpiredToken(response);
            return;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            jwtExceptionHandler.handleInvalidToken(response);
            return;
        } catch (Exception e) {
            log.error("Error processing JWT token", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
