package com.mvcCore.controller;

import com.mvcCore.dto.ApiResponse;
import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.AdminLoginRequest;
import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.UserDto;
import com.mvcCore.exception.UnauthorizedException;
import com.mvcCore.service.AuthService;
import com.mvcCore.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.debug("Register request for email: {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Login request for email: {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        log.debug("Admin login request for username: {}", request.getUsername());
        AuthResponse response = authService.adminLogin(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMe(@RequestHeader("Authorization") String authHeader) {
        log.debug("Get me request");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized access attempt: missing or invalid auth header");
            throw new UnauthorizedException("Missing or invalid authorization header");
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtUtil.validateToken(token)) {
            log.warn("Unauthorized access attempt: invalid token");
            throw new UnauthorizedException("Invalid or expired token");
        }
        
        UserDto user = authService.getMe(token);
        if (user == null) {
            log.error("User not found from token");
            throw new UnauthorizedException("User not found");
        }
        
        return ResponseEntity.ok(
            new ApiResponse<>("User info retrieved successfully", user, true)
        );
    }
}