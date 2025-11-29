package com.mvcCore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be blank")
    private String email;
    
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;
}