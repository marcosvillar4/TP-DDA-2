package com.example.DA2Back.config;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.DA2Back.Seguridad.negocio.CustomUserDetailsService;

/**
 * Configuracion central de Spring Security.
 *
 * Endpoints publicos:
 *   - /api/auth/**       → registro e inicio de sesion
 *   - /auth/**           → compatibilidad con AdminSeeder/compañeros
 *   - /actuator/**       → health checks y monitoreo
 *   - /v3/api-docs/**    → documentacion OpenAPI
 *   - /swagger-ui/**     → UI de Swagger
 *
 * Endpoints protegidos (requieren Bearer JWT valido):
 *   - /api/pedidos/**
 *   - /api/comercios/**
 *   - Cualquier otro endpoint no listado arriba
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth

                // Publicos — autenticacion (ambos prefijos para compatibilidad)
                .requestMatchers(
                        "/api/auth/**",
                        "/auth/**",
                        "/actuator/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                // Admin
                .requestMatchers("/usuarios/admin/**").hasRole("ADMIN")

                // Protegidos — requieren JWT valido
                .requestMatchers("/api/pedidos/**").authenticated()
                .requestMatchers("/api/comercios/**").authenticated()

                // Cualquier otro endpoint tambien requiere autenticacion
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}