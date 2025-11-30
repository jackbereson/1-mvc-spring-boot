package com.mvcCore.service;

import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.UserDto;

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
     * @throws com.mvcCore.exception.BadRequestException if email already exists
     */
    AuthResponse register(RegisterRequest request);
    
    /**
     * Authenticates a user and provides tokens.
     *
     * @param request login request with credentials
     * @return authentication response with tokens
     * @throws com.mvcCore.exception.UnauthorizedException if credentials are invalid
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
     * @throws com.mvcCore.exception.UnauthorizedException if refresh token is invalid
     */
    AuthResponse refreshToken(String refreshToken);
}