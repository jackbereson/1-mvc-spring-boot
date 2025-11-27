package com.mvcCore.service;

import com.mvcCore.dto.RegisterRequest;
import com.mvcCore.dto.LoginRequest;
import com.mvcCore.dto.AuthResponse;
import com.mvcCore.dto.UserDto;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDto getMe(String token);
}