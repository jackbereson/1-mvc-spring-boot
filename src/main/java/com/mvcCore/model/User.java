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
    
    @Column(unique = true)
    private String username;
    
    @Column(unique = true)
    private String email;
    
    private String fullName;
    
    private String phoneNumber;
    
    private String password;
    
    @Column(columnDefinition = "boolean default true")
    @Builder.Default
    private Boolean isActive = true;
    
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'USER'")
    @Builder.Default
    private Role role = Role.USER;
}