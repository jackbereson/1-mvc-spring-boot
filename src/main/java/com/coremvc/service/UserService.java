package com.coremvc.service;

import com.coremvc.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for user management operations.
 * <p>
 * Defines business logic methods for user CRUD operations.
 * Implementations handle data access and business rules.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
public interface UserService {
    /**
     * Retrieves all users without pagination.
     *
     * @return list of all users
     */
    List<UserDto> getAllUsers();
    
    /**
     * Retrieves all users with pagination and sorting.
     *
     * @param pageable pagination and sorting parameters
     * @return paginated list of users
     */
    Page<UserDto> getAllUsers(Pageable pageable);
    
    /**
     * Retrieves a specific user by ID.
     *
     * @param id the user ID
     * @return user data transfer object
     * @throws com.coremvc.exception.ResourceNotFoundException if user not found
     */
    UserDto getUserById(Long id);
    
    /**
     * Updates an existing user.
     *
     * @param id the user ID to update
     * @param userDto user data with updated fields
     * @return updated user data transfer object
     * @throws com.coremvc.exception.ResourceNotFoundException if user not found
     */
    UserDto updateUser(Long id, UserDto userDto);
    
    /**
     * Deletes a user by ID.
     *
     * @param id the user ID to delete
     * @throws com.coremvc.exception.ResourceNotFoundException if user not found
     */
    void deleteUser(Long id);
}
