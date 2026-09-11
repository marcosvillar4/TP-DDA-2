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
            AuthenticationConfiguration config
    ) throws Exception {

        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
            // CORS
            .cors(cors -> {})

            // CSRF no es necesario utilizando JWT
            .csrf(csrf -> csrf.disable())

            // No utilizamos sesiones
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            // Proveedor de autenticación
            .authenticationProvider(authenticationProvider())

            // Autorización de endpoints
            .authorizeHttpRequests(auth -> auth

    .requestMatchers("/auth/**").permitAll()

    .requestMatchers("/usuarios/admin/**")
        .hasRole("ADMIN")

    .requestMatchers("/comercio/**")
        .hasRole("COMERCIO")

    .requestMatchers("/repartidor/**")
        .hasRole("REPARTIDOR")

    .requestMatchers("/repartidores/**")
        .hasAnyRole("ADMIN", "REPARTIDOR", "DEPOSITO")
    
    .requestMatchers("/deposito/**")
        .hasAnyRole("DEPOSITO", "ADMIN", "COMERCIO")

    .anyRequest().authenticated()
)

            // Filtro JWT antes del filtro de usuario/contraseña
            .addFilterBefore(
                    jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
