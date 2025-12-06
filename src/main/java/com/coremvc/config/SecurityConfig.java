package com.coremvc.config;

import com.coremvc.security.JwtAuthenticationEntryPoint;
import com.coremvc.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the application.
 * <p>
 * This class configures Spring Security 6+ with JWT-based authentication
 * following modern best practices (2024-2025).
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Stateless session management (no server-side sessions)</li>
 *   <li>JWT-based authentication with custom filter</li>
 *   <li>Public endpoints configured via permitAll()</li>
 *   <li>Standardized error handling via AuthenticationEntryPoint</li>
 *   <li>Method-level security enabled with @PreAuthorize</li>
 * </ul>
 * </p>
 *
 * @author MVC Core Team
 * @version 2.0.0
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtFilter jwtFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


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
     * Configures the security filter chain using Spring Security 6+ best practices.
     * <p>
     * Configuration details:
     * <ul>
     *   <li><b>CSRF:</b> Disabled (stateless REST API)</li>
     *   <li><b>CORS:</b> Disabled (configure separately if needed)</li>
     *   <li><b>Session Management:</b> Stateless (no server-side sessions)</li>
     *   <li><b>Public endpoints:</b> /api/auth/**, /api/v1/auth/**, /api/v1/health/**</li>
     *   <li><b>Protected endpoints:</b> All other requests require authentication</li>
     *   <li><b>Exception handling:</b> Custom AuthenticationEntryPoint for 401 errors</li>
     *   <li><b>JWT Filter:</b> Runs before standard authentication filter</li>
     * </ul>
     * </p>
     * <p>
     * Note: Path-based access control is handled here via permitAll(),
     * not in the JWT filter. This is the recommended Spring Security approach.
     * </p>
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/auth/refresh",
                                "/api/v1/health/**",
                                "/api/v1/settings/default"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings.
     * <p>
     * Allows frontend application (localhost:3000) to make requests to the API.
     * Configured to allow all common HTTP methods and headers.
     * </p>
     *
     * @return CorsConfigurationSource with allowed origins, methods, and headers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}