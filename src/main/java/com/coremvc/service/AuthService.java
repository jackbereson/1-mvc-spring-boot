package com.coremvc.service;

import com.coremvc.dto.RegisterRequest;
import com.coremvc.dto.LoginRequest;
import com.coremvc.dto.AuthResponse;
import com.coremvc.dto.UserDto;

/**
 * Service interface for authentication operations.
 * <p>
 * Defines methods for user registration, login, profile retrieval,
 * and token refresh operations.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface AuthService {
    /**
     * Registers a new user account.
     *
     * @param request registration request with user details
     * @return authentication response with tokens
     * @throws com.coremvc.exception.BadRequestException if email already exists
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Authenticates a user and provides tokens.
     *
     * @param request login request with credentials
     * @return authentication response with tokens
     * @throws com.coremvc.exception.UnauthorizedException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);
    
    /**
     * Retrieves user profile information.
     *
     * @param uuid the user's unique identifier
     * @return user data transfer object
     */
    UserDto getMe(String uuid);
    
    /**
     * Refreshes an expired access token.
     *
     * @param refreshToken the refresh token
     * @return authentication response with new tokens
     * @throws com.coremvc.exception.UnauthorizedException if refresh token is invalid
     */
    AuthResponse refreshToken(String refreshToken);
}