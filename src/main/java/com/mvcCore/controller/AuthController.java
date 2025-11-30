package com.mvcCore.controller;

import com.mvcCore.dto.ApiResponse;
import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.RefreshTokenRequest;
import com.mvcCore.dto.UserDto;
import com.mvcCore.exception.UnauthorizedException;
import com.mvcCore.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * REST controller for authentication operations.
 * <p>
 * This controller handles user registration, login, token refresh,
 * and user profile retrieval. All endpoints are publicly accessible
 * except for the profile endpoint which requires authentication.
 * Base path: /api/v1/auth
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * Registers a new user account.
     * <p>
     * Creates a new user with the provided information and returns
     * JWT access and refresh tokens. The endpoint is publicly accessible.
     * </p>
     *
     * @param request the registration request containing user details
     * @return ResponseEntity with authentication response including tokens
     * @throws com.mvcCore.exception.BadRequestException if email already exists
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Authenticates a user and provides JWT tokens.
     * <p>
     * Supports both regular user login (via email) and admin login (via username).
     * Returns access token and refresh token upon successful authentication.
     * </p>
     *
     * @param request the login request containing credentials (email/username and password)
     * @return ResponseEntity with authentication response including tokens
     * @throws com.mvcCore.exception.UnauthorizedException if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Refreshes an expired access token using a refresh token.
     * <p>
     * Validates the provided refresh token and generates new access
     * and refresh tokens if valid. This allows users to maintain
     * their session without re-entering credentials.
     * </p>
     *
     * @param request the refresh token request
     * @return ResponseEntity with new authentication tokens
     * @throws com.mvcCore.exception.UnauthorizedException if refresh token is invalid or expired
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retrieves the authenticated user's profile information.
     * <p>
     * Requires valid JWT token in Authorization header.
     * Returns detailed information about the currently logged-in user.
     * </p>
     *
     * @return ResponseEntity containing the user's profile data
     * @throws com.mvcCore.exception.UnauthorizedException if user not authenticated or not found
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMe() {
        String uuid = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (uuid == null || uuid.isEmpty()) {
            throw new UnauthorizedException("User not authenticated");
        }
        
        System.out.println("Getting user profile for uuid: " + uuid);

        UserDto user = authService.getMe(uuid);
        if (user == null) {
            throw new UnauthorizedException("User not found");
        }
        
        return ResponseEntity.ok(
            new ApiResponse<>("User info retrieved successfully", user, true)
        );
    }
}