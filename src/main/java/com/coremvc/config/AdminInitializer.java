package com.coremvc.config;

import com.coremvc.model.Role;
import com.coremvc.model.User;
import com.coremvc.repository.UserRepository;
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
