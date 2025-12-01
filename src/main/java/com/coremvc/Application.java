package com.coremvc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the MVC Core Spring Boot application.
 * <p>
 * This class bootstraps the Spring Boot application with auto-configuration enabled.
 * It scans for components, configurations, and services in the com.coremvc package.
 * </p>
 *
 * @author MVC Core Team
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
public class Application {
    /**
     * Main method to start the Spring Boot application.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
