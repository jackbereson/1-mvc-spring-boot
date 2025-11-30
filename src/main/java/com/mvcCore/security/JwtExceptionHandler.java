package com.mvcCore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT Exception Handler for handling JWT-specific errors.
 * <p>
 * This component provides centralized handling for JWT token errors
 * such as expired tokens and invalid tokens. It generates standardized
 * JSON error responses for better client-side error handling.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtExceptionHandler {

    private final ObjectMapper objectMapper;

    /**
     * Handles expired JWT token errors.
     * <p>
     * Returns a 401 response with TOKEN_EXPIRED error code,
     * instructing the client to use the refresh token.
     * </p>
     *
     * @param response the HTTP response
     * @throws IOException if writing response fails
     */
    public void handleExpiredToken(HttpServletResponse response) throws IOException {
        writeError(response, "TOKEN_EXPIRED", "Token has expired. Please use refresh token to get a new token.");
    }

    /**
     * Handles invalid JWT token errors.
     * <p>
     * Returns a 401 response with INVALID_TOKEN error code.
     * This includes malformed tokens, invalid signatures, etc.
     * </p>
     *
     * @param response the HTTP response
     * @throws IOException if writing response fails
     */
    public void handleInvalidToken(HttpServletResponse response) throws IOException {
        writeError(response, "INVALID_TOKEN", "Invalid token. Please login again.");
    }

    /**
     * Writes a standardized JSON error response.
     * <p>
     * Helper method to construct and write error responses
     * with consistent structure across all JWT errors.
     * </p>
     *
     * @param response the HTTP response
     * @param error the error code
     * @param message the error message
     * @throws IOException if writing response fails
     */
    private void writeError(HttpServletResponse response, String error, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        errorDetails.put("status", HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
