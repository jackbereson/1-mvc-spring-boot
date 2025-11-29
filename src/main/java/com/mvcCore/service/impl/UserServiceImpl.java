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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(Pageable pageable) {
        log.debug("Fetching users with pagination: {}", pageable);
        return userRepository.findAll(pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.debug("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });
        return userMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        log.debug("Updating user with id: {}", id);
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

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new ResourceNotFoundException("User", "id", id);
                });

        userRepository.delete(user);
        log.info("User with id {} deleted successfully", id);
    }
}
