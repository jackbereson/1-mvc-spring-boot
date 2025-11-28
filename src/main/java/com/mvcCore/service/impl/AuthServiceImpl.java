package com.mvcCore.service.impl;

import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.AdminLoginRequest;
import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.UserDto;
import com.mvcCore.model.Role;
import com.mvcCore.model.User;
import com.mvcCore.repository.UserRepository;
import com.mvcCore.service.AuthService;
import com.mvcCore.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${admin.username}")
    private String adminUsername;
    
    @Value("${admin.password}")
    private String adminPassword;
    
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            return AuthResponse.builder()
                    .message("Email already exists")
                    .build();
        }
        
        User user = User.builder()
                .email(request.getEmail())
                .fullName(request.getFullName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isActive(true)
                .build();
        
        userRepository.save(user);
        
        String token = jwtUtil.generateToken(user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Register successfully")
                .build();
    }
    
    @Override
    public AuthResponse login(LoginRequest request) {
        User user = null;
        
        // Try login by email first
        if (request.getEmail() != null) {
            user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
        }
        
        // If not found and username provided, try by email using username as hint
        if (user == null && request.getUsername() != null) {
            user = userRepository.findByEmail(request.getUsername())
                    .orElse(null);
        }
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .message("Invalid email or password")
                    .build();
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .message("Login successfully")
                .build();
    }
    
    @Override
    public AuthResponse adminLogin(AdminLoginRequest request) {
        if (!adminUsername.equals(request.getUsername()) || !adminPassword.equals(request.getPassword())) {
            return AuthResponse.builder()
                    .message("Invalid admin credentials")
                    .build();
        }
        
        String token = jwtUtil.generateToken("admin_" + adminUsername);
        
        return AuthResponse.builder()
                .token(token)
                .email("admin@system.local")
                .fullName("Admin")
                .message("Admin login successfully")
                .build();
    }
    
    @Override
    public UserDto getMe(String token) {
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
            return null;
        }
        
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .isActive(user.getIsActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

