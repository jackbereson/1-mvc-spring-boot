package com.mvcCore.service;

import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.AdminLoginRequest;
import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.UserDto;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse adminLogin(AdminLoginRequest request);
    UserDto getMe(String token);
}