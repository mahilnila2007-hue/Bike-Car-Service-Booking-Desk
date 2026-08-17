package com.garage.management.config;

import com.garage.management.security.FirebaseAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final FirebaseAuthFilter firebaseAuthFilter;

    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/", "/index.html",
                    "/login.html", "/register.html",
                    "/css/**", "/js/**", "/images/**",
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/api-docs/**", "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()

                // Auth endpoints - public for initial profile creation
                .requestMatchers(HttpMethod.POST, "/api/auth/profile").authenticated()
                .requestMatchers("/api/auth/**").authenticated()

                // Customer endpoints
                .requestMatchers("/api/vehicles/**").authenticated()
                .requestMatchers("/api/bookings/**").authenticated()

                // Staff/Admin only
                .requestMatchers("/api/bays/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/mechanics/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/parts/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/bills/**").hasAnyRole("STAFF", "ADMIN", "CUSTOMER")
                .requestMatchers("/api/service-types/**").hasAnyRole("STAFF", "ADMIN")

                // Admin only
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasAnyRole("ADMIN", "STAFF")
                .requestMatchers("/api/seed/**").hasRole("ADMIN")

                // Service jobs - authenticated (role checks in service layer)
                .requestMatchers("/api/service-jobs/**").authenticated()

                // SSE - authenticated
                .requestMatchers("/api/sse/**").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
