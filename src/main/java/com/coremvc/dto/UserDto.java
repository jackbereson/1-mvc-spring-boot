package com.coremvc.dto;

import com.coremvc.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String uuid;
    private String email;
    private String fullName;
    private Boolean isActive;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}