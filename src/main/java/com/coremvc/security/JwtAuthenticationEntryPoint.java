package com.coremvc.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point for handling unauthorized access.
 * <p>
 * This component implements Spring Security's AuthenticationEntryPoint interface
 * to provide standardized 401 error responses for unauthorized requests.
 * It returns a JSON response with error details instead of redirecting to a login page.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Handles unauthorized access attempts.
     * <p>
     * This method is triggered when a user tries to access a protected resource
     * without proper authentication. It returns a JSON response with 401 status.
     * </p>
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the authentication exception
     * @throws IOException if writing response fails
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", "UNAUTHORIZED");
        errorDetails.put("message", "Access denied. Authentication required.");
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorDetails.put("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
