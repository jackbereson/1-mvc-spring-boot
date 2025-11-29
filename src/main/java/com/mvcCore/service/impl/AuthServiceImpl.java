package com.mvcCore.service.impl;

import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.AdminLoginRequest;
import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.UserDto;
import com.mvcCore.exception.BadRequestException;
import com.mvcCore.exception.UnauthorizedException;
import com.mvcCore.mapper.UserMapper;
import com.mvcCore.model.Role;
import com.mvcCore.model.User;
import com.mvcCore.repository.UserRepository;
import com.mvcCore.service.AuthService;
import com.mvcCore.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        log.debug("Attempting to register user with email: {}", request.getEmail());
        
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
        String token = jwtUtil.generateToken(user.getEmail());
        
        log.info("User registered successfully: {}", user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Register successfully")
                .build();
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        log.debug("Attempting to login user with email: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: invalid email or password - {}", request.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        log.info("User logged in successfully: {}", user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Login successfully")
                .build();
    }
    
    @Override
    public AuthResponse adminLogin(AdminLoginRequest request) {
        log.debug("Attempting admin login with username: {}", request.getUsername());
        
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            log.warn("Admin login failed: invalid credentials");
            throw new UnauthorizedException("Invalid admin credentials");
        }
        
        String token = jwtUtil.generateToken("admin_" + adminUsername);
        log.info("Admin logged in successfully");
        
        return AuthResponse.builder()
                .token(token)
                .email("admin@system.local")
                .fullName("Admin")
                .message("Admin login successfully")
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDto getMe(String token) {
        log.debug("Fetching user info from token");
        String email = jwtUtil.extractUsername(token);
        
        // Check if it's an admin token
        if (email.startsWith("admin_")) {
            return UserDto.builder()
                    .email("admin@system.local")
                    .fullName("Admin")
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
        }
        
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            log.error("User not found: {}", email);
            return null;
        }
        
        return userMapper.toDto(user);
    }
}

