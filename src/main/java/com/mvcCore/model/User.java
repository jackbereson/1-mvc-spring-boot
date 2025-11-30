package com.mvcCore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * User entity representing a system user.
 * <p>
 * Maps to the 'users' table in the database.
 * Contains user authentication and profile information.
 * Extends BaseEntity for common fields like id, createdAt, and updatedAt.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    /** Unique identifier for the user (UUID format) */
    @Column(unique = true, nullable = false, columnDefinition = "VARCHAR(36)")
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();
    
    /** Username for login (optional, primarily for admin) */
    @Column(unique = true)
    private String username;
    
    /** Email address (unique, used for user login) */
    @Column(unique = true)
    private String email;
    
    /** User's full name */
    private String fullName;
    
    /** User's phone number */
    private String phoneNumber;
    
    /** Hashed password (BCrypt) */
    private String password;
    
    /** Account active status */
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;
    
    /** User role (USER or ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'USER'")
    @Builder.Default
    private Role role = Role.USER;
}