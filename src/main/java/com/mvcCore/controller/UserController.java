package com.mvcCore.controller;

import com.mvcCore.dto.ApiResponse;
import com.mvcCore.dto.UserDto;
import com.mvcCore.dto.request.UpdateUserRequest;
import com.mvcCore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    
    private final UserService userService;
    
    // GET all users
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserDto>>> getAllUsers() {
        log.debug("Get all users request");
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(
            new ApiResponse<>("Users retrieved successfully", users, true)
        );
    }
    
    // GET user by id
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        log.debug("Get user by id request: {}", id);
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(
            new ApiResponse<>("User retrieved successfully", user, true)
        );
    }
    
    // UPDATE user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.debug("Update user request: {}", id);
        
        UserDto userDto = UserDto.builder()
                .fullName(request.getFullName())
                .isActive(request.getIsActive())
                .build();
        
        UserDto updatedUser = userService.updateUser(id, userDto);
        return ResponseEntity.ok(
            new ApiResponse<>("User updated successfully", updatedUser, true)
        );
    }
    
    // DELETE user
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.debug("Delete user request: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(
            new ApiResponse<>("User deleted successfully", null, true)
        );
    }
}
