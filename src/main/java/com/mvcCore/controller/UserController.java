package com.mvcCore.controller;

import com.mvcCore.dto.ApiResponse;
import com.mvcCore.dto.UserDto;
import com.mvcCore.dto.request.UpdateUserRequest;
import com.mvcCore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    // GET all users with pagination
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {
        
        Sort.Direction direction = sortDirection.equalsIgnoreCase("DESC") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<UserDto> users = userService.getAllUsers(pageable);
        
        return ResponseEntity.ok(
            new ApiResponse<>("Users retrieved successfully", users, true)
        );
    }
    
    // GET user by id
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(
            new ApiResponse<>("User retrieved successfully", user, true)
        );
    }
    
    // UPDATE user
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
            new ApiResponse<>("User deleted successfully", null, true)
        );
    }
}
