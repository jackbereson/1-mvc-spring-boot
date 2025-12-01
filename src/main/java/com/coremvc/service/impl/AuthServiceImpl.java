package com.coremvc.service.impl;

import com.coremvc.dto.AuthResponse;
import com.coremvc.dto.LoginRequest;
import com.coremvc.dto.RegisterRequest;
import com.coremvc.dto.UserDto;
import com.coremvc.exception.BadRequestException;
import com.coremvc.exception.UnauthorizedException;
import com.coremvc.mapper.UserMapper;
import com.coremvc.model.Role;
import com.coremvc.model.User;
import com.coremvc.repository.UserRepository;
import com.coremvc.service.AuthService;
import com.coremvc.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of AuthService interface.
 * <p>
 * Handles authentication operations including user registration,
 * login (both user and admin), profile retrieval, and token refresh.
 * Uses JWT for token generation and BCrypt for password hashing.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    
    @Value("${admin.username}")
    private String adminUsername;
    
    @Value("${admin.password}")
    private String adminPassword;
    
    /**
     * {@inheritDoc}
     * <p>
     * Validates that the email is not already in use, creates a new user
     * with hashed password, and generates JWT tokens.
     * </p>
     */
    @Override
    public AuthResponse register(RegisterRequest request) {
        
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: email already exists - {}", request.getEmail());
            throw new BadRequestException("Email already exists");
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.USER)
                .isActive(true)
                .build();
        
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUuid(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUuid());
        
        log.info("User registered successfully: {}", user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Register successfully")
                .build();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Supports two login modes:
     * <ul>
     *   <li>Admin login: uses username and password from application properties</li>
     *   <li>User login: validates email and password against database</li>
     * </ul>
     * Generates JWT access and refresh tokens on successful authentication.
     * </p>
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        // Check if it's admin login
        if (request.getUsername() != null && !request.getUsername().isEmpty()) {
            if (adminUsername.equals(request.getUsername()) && adminPassword.equals(request.getPassword())) {
                String token = jwtUtil.generateToken("admin-uuid", "admin@system.local", "ADMIN");
                String refreshToken = jwtUtil.generateRefreshToken("admin-uuid");
                log.info("Admin logged in successfully");
                
                return AuthResponse.builder()
                        .token(token)
                        .refreshToken(refreshToken)
                        .email("admin@system.local")
                        .fullName("Admin")
                        .message("Login successfully")
                        .build();
            } else {
                log.warn("Admin login failed: invalid credentials");
                throw new UnauthorizedException("Invalid credentials");
            }
        }
        
        // Regular user login
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new BadRequestException("Email is required for user login");
        }
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid email or password - {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getUuid(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUuid());
        log.info("User logged in successfully: {}", user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Login successfully")
                .build();
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Returns a hardcoded admin profile for admin UUID,
     * or retrieves regular user data from the database.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getMe(String uuid) {
        // Check if it's an admin token
        if ("admin-uuid".equals(uuid)) {
            return UserDto.builder()
                    .email("admin@system.local")
                    .fullName("Admin")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
        }

        log.info("Getting user profile for uuid: {}", uuid);
        
        User user = userRepository.findByUuid(uuid)
                .orElse(null);
        
        if (user == null) {
            log.error("User not found with uuid: {}", uuid);
            return null;
        }
        
        return userMapper.toDto(user);
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Validates the refresh token, extracts user information,
     * and generates new access and refresh tokens. Handles both
     * admin and regular user token refresh.
     * </p>
     */
    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            log.warn("Refresh token validation failed");
            throw new UnauthorizedException("Invalid or expired refresh token");
        }
        
        String uuid = jwtUtil.extractUuid(refreshToken);
        
        // Check if it's an admin token
        if ("admin-uuid".equals(uuid)) {
            String newToken = jwtUtil.generateToken("admin-uuid", "admin@system.local", "ADMIN");
            String newRefreshToken = jwtUtil.generateRefreshToken("admin-uuid");
            
            return AuthResponse.builder()
                    .token(newToken)
                    .refreshToken(newRefreshToken)
                    .email("admin@system.local")
                    .fullName("Admin")
                    .message("Token refreshed successfully")
                    .build();
        }
        
        User user = userRepository.findByUuid(uuid)
                .orElse(null);
        
        if (user == null || !user.getIsActive()) {
            log.warn("User not found or inactive: {}", uuid);
            throw new UnauthorizedException("User not found or inactive");
        }
        
        String newToken = jwtUtil.generateToken(user.getUuid(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getUuid());
        
        log.info("Token refreshed successfully for user: {}", uuid);
        
        return AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Token refreshed successfully")
                .build();
    }
}

