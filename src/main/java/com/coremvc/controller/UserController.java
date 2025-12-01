package com.coremvc.controller;

import com.coremvc.dto.ApiResponse;
import com.coremvc.dto.UserDto;
import com.coremvc.dto.request.UpdateUserRequest;
import com.coremvc.service.UserService;
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

/**
 * REST controller for user management operations.
 * <p>
 * This controller provides endpoints for CRUD operations on users.
 * All endpoints require ADMIN role for access.
 * Base path: /api/v1/users
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * Retrieves all users with pagination and sorting.
     * <p>
     * Requires ADMIN role. Returns paginated list of users with configurable
     * page size, page number, sort field, and sort direction.
     * </p>
     *
     * @param page the page number (zero-based, default: 0)
     * @param size the page size (default: 10)
     * @param sortBy the field to sort by (default: "id")
     * @param sortDirection the sort direction - ASC or DESC (default: "ASC")
     * @return ResponseEntity containing paginated user list
     */
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
    
    /**
     * Retrieves a specific user by ID.
     * <p>
     * Requires ADMIN role. Returns detailed information about a single user.
     * </p>
     *
     * @param id the user ID
     * @return ResponseEntity containing the user data
     * @throws com.coremvc.exception.ResourceNotFoundException if user not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(
            new ApiResponse<>("User retrieved successfully", user, true)
        );
    }
    
    /**
     * Updates an existing user.
     * <p>
     * Requires ADMIN role. Updates user's full name and active status.
     * Only provided fields will be updated.
     * </p>
     *
     * @param id the user ID to update
     * @param request the update request containing new user data
     * @return ResponseEntity containing the updated user data
     * @throws com.coremvc.exception.ResourceNotFoundException if user not found
     */
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
    
    /**
     * Deletes a user by ID.
     * <p>
     * Requires ADMIN role. Permanently removes the user from the system.
     * </p>
     *
     * @param id the user ID to delete
     * @return ResponseEntity with success message
     * @throws com.coremvc.exception.ResourceNotFoundException if user not found
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
            new ApiResponse<>("User deleted successfully", null, true)
        );
    }
}
