package com.mvcCore.service;

import com.mvcCore.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    
    Page<UserDto> getAllUsers(Pageable pageable);
    
    UserDto getUserById(Long id);
    
    UserDto updateUser(Long id, UserDto userDto);
    
    void deleteUser(Long id);
}
