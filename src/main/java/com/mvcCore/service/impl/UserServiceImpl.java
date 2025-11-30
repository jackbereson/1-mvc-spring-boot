package com.mvcCore.service.impl;

import com.mvcCore.dto.UserDto;
import com.mvcCore.exception.ResourceNotFoundException;
import com.mvcCore.mapper.UserMapper;
import com.mvcCore.model.User;
import com.mvcCore.repository.UserRepository;
import com.mvcCore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UserService interface.
 * <p>
 * Provides concrete implementations for user management operations
 * including retrieval, update, and deletion. Uses UserMapper for
 * entity-DTO conversions and handles transactional operations.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves all users and maps them to DTOs using Java Streams.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses Spring Data's Page interface for efficient pagination.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Logs error if user not found.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });
        return userMapper.toDto(user);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Only updates non-null fields from the DTO. Logs successful updates.
     * </p>
     */
    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        if (userDto.getFullName() != null) {
            user.setFullName(userDto.getFullName());
        }
        if (userDto.getIsActive() != null) {
            user.setIsActive(userDto.getIsActive());
        }

        User updatedUser = userRepository.save(user);
        log.info("User with id {} updated successfully", id);
        return userMapper.toDto(updatedUser);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Permanently deletes the user from the database. Logs successful deletions.
     * </p>
     */
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        userRepository.delete(user);
        log.info("User with id {} deleted successfully", id);
    }
}
