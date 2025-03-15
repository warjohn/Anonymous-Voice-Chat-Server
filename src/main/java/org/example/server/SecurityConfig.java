package org.example.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration class to define security settings for the application.
 * This class is annotated with @Configuration, indicating that it contains Spring configuration.
 */
@Configuration // Marks this class as a configuration class, enabling it to define beans and other settings
public class SecurityConfig {

    /**
     * Defines a SecurityFilterChain bean to configure HTTP security settings.
     * This method disables CSRF protection and permits all requests without authentication.
     *
     * @param http the HttpSecurity object used to configure security settings
     * @return a configured SecurityFilterChain bean
     * @throws Exception if an error occurs during configuration
     */
    @Bean // Marks this method as a bean definition, making the SecurityFilterChain managed by Spring
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configure authorization rules for incoming HTTP requests
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // Allow all requests without requiring authentication
                )
                // Disable CSRF (Cross-Site Request Forgery) protection
                .csrf(csrf -> csrf.disable());

        return http.build(); // Build and return the configured SecurityFilterChain
    }
}