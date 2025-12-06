package com.coremvc.service.impl;

import com.coremvc.dto.RestPage;
import com.coremvc.dto.UserDto;
import com.coremvc.exception.ResourceNotFoundException;
import com.coremvc.mapper.UserMapper;
import com.coremvc.model.User;
import com.coremvc.repository.UserRepository;
import com.coremvc.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
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
     * Cached with short TTL for security.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "'all'")
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses Spring Data's Page interface for efficient pagination.
     * Cached with short TTL for security.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<UserDto> getAllUsers(Pageable pageable) {
        Page<UserDto> page = userRepository.findAll(pageable)
                .map(userMapper::toDto);
        return new RestPage<>(page.getContent(), pageable, page.getTotalElements());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Logs error if user not found.
     * Cached with short TTL (7 min) for security.
     * </p>
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
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
     * Invalidates user cache on update.
     * </p>
     */
    @Override
    @CachePut(value = "users", key = "#id")
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
     * Invalidates user cache on delete.
     * </p>
     */
    @Override
    @CacheEvict(value = "users", allEntries = true)
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
