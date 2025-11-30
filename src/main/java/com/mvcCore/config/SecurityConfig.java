package com.mvcCore.config;

import com.mvcCore.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * Security configuration for the application.
 * <p>
 * This class configures Spring Security with JWT-based authentication.
 * It defines which endpoints are publicly accessible and which require authentication.
 * Method-level security is enabled with {@code @PreAuthorize} annotations.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final JwtFilter jwtFilter;

    /**
     * Constructor for SecurityConfig.
     *
     * @param jwtFilter the JWT filter for token validation
     */
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }


    /**
     * Provides a password encoder bean using BCrypt hashing algorithm.
     * <p>
     * BCrypt is a strong hashing function designed for password storage.
     * It automatically handles salt generation and is computationally expensive
     * to resist brute-force attacks.
     * </p>
     *
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    

    /**
     * Configures the security filter chain.
     * <p>
     * This method sets up:
     * <ul>
     *   <li>CSRF protection (disabled for REST API)</li>
     *   <li>CORS configuration (disabled)</li>
     *   <li>Public endpoints: /api/auth/**, /api/v1/auth/**, /api/v1/health/**, /h2-console/**</li>
     *   <li>All other endpoints require authentication</li>
     *   <li>JWT filter added before standard authentication filter</li>
     * </ul>
     * </p>
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/api/auth/**"),
                                new AntPathRequestMatcher("/api/v1/auth/**"),
                                new AntPathRequestMatcher("/api/v1/health/**")
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}