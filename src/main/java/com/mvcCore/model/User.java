package com.mvcCore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String fullName;
    
    @Column(nullable = false)
    private String password;
    
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'USER'")
    @Builder.Default
    private Role role = Role.USER;
}