package com.mvcCore.config;

import com.mvcCore.model.Role;
import com.mvcCore.model.User;
import com.mvcCore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${admin.init:false}")
    private Boolean adminInit;
    
    @Value("${admin.username:admin}")
    private String adminUsername;
    
    @Value("${admin.password:admin123}")
    private String adminPassword;
    
    @Override
    public void run(String... args) throws Exception {
        if (adminInit) {
            // Delete existing admin user if exists
            userRepository.findByEmail("admin@system.local").ifPresent(user -> {
                userRepository.delete(user);
            });
            
            // Create new admin user
            User adminUser = User.builder()
                    .email("admin@system.local")
                    .username(adminUsername)
                    .fullName("System Administrator")
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            
            userRepository.save(adminUser);
            System.out.println("✓ Admin user initialized successfully");
            System.out.println("  Username: " + adminUsername);
            System.out.println("  Email: admin@system.local");
        }
    }
}
