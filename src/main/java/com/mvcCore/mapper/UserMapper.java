package com.mvcCore.mapper;

import com.mvcCore.dto.UserDto;
import com.mvcCore.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for User entity and UserDto conversions.
 * <p>
 * Automatically generates implementation code for entity-DTO mappings.
 * Configured for Spring component model for dependency injection.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Converts User entity to UserDto.
     *
     * @param user the User entity
     * @return UserDto data transfer object
     */
    UserDto toDto(User user);

    /**
     * Converts UserDto to User entity.
     * <p>
     * Note: Sensitive fields (username, password, phoneNumber) are not mapped
     * from DTO to prevent unauthorized updates.
     * </p>
     *
     * @param userDto the UserDto data transfer object
     * @return User entity (with username, password, phoneNumber ignored)
     */
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "phoneNumber", ignore = true)
    User toEntity(UserDto userDto);
}
