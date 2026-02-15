package com.mindterminal.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // <--- NEW IMPORT
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity // <--- THIS WAS MISSING
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simple API usage
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to the frontend files
                        .requestMatchers("/", "/index.html", "/style.css", "/app.js", "/manifest.json").permitAll()
                        // Lock down the API (backend) to logged-in users only
                        .requestMatchers("/api/**").authenticated()
                )
                .httpBasic(withDefaults()); // Use standard Browser Login Popup

        return http.build();
    }
}