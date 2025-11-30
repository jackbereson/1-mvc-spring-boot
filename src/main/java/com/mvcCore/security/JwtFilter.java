package com.mvcCore.security;

import com.mvcCore.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT authentication filter.
 * <p>
 * Intercepts HTTP requests and validates JWT tokens from the Authorization header.
 * Extracts user information from valid tokens and sets up Spring Security context.
 * Handles expired and invalid tokens with appropriate error responses.
 * Public endpoints are excluded from JWT validation.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Filters incoming HTTP requests and validates JWT tokens.
     * <p>
     * Process flow:
     * <ol>
     *   <li>Skip JWT validation for public endpoints</li>
     *   <li>Extract JWT from Authorization header</li>
     *   <li>Validate token and extract user information</li>
     *   <li>Set up Spring Security authentication context</li>
     *   <li>Handle token expiration and validation errors</li>
     * </ol>
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
        
        String requestPath = request.getRequestURI();
        
        // Skip JWT validation for public endpoints (but NOT /me endpoint)
        if (requestPath.startsWith("/api/auth/") || 
            (requestPath.startsWith("/api/v1/auth/") && !requestPath.equals("/api/v1/auth/me")) || 
            requestPath.startsWith("/api/v1/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                try {
                    if (jwtUtil.validateToken(token)) {
                        String uuid = jwtUtil.extractUuid(token);
                        String role = jwtUtil.extractRole(token);
                        
                        List<SimpleGrantedAuthority> authorities = role != null ? 
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role)) : 
                                Collections.emptyList();
                        
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(uuid, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.warn("JWT token validation failed");
                    }
                } catch (ExpiredJwtException e) {
                    log.warn("JWT token has expired");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    
                    Map<String, Object> errorDetails = new HashMap<>();
                    errorDetails.put("error", "TOKEN_EXPIRED");
                    errorDetails.put("message", "Token has expired. Please use refresh token to get a new token.");
                    errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                    
                    response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
                    return;
                } catch (JwtException e) {
                    log.warn("Invalid JWT token: {}", e.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    
                    Map<String, Object> errorDetails = new HashMap<>();
                    errorDetails.put("error", "INVALID_TOKEN");
                    errorDetails.put("message", "Invalid token");
                    errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
                    
                    response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
                    return;
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
