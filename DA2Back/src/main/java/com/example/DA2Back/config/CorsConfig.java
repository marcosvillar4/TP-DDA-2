package com.example.DA2Back.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración CORS (B2 — Seguridad de la aplicación):
 *  - Orígenes explícitos configurables por entorno (cors.allowed-origins),
 *    sin comodines (*) ni rangos privados abiertos cuando allowCredentials=true.
 */
@Configuration
public class CorsConfig {

    public static final String DEFAULT_ALLOWED_ORIGINS =
            "http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:3000";

    @Value("${cors.allowed-origins:" + DEFAULT_ALLOWED_ORIGINS + "}")
    private String allowedOrigins = DEFAULT_ALLOWED_ORIGINS;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        List<String> origenes = parsearYValidarOrigenes(allowedOrigins);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origenes);

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Idempotency-Key",
                "If-Match"
        ));

        configuration.setExposedHeaders(List.of(
                "Retry-After",
                "ETag",
                "WWW-Authenticate"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    public static List<String> parsearYValidarOrigenes(String rawOrigins) {
        if (rawOrigins == null || rawOrigins.isBlank()) {
            throw new IllegalStateException("cors.allowed-origins debe definir al menos un origen explícito");
        }
        List<String> origenes = Arrays.stream(rawOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (origenes.isEmpty()) {
            throw new IllegalStateException("cors.allowed-origins debe definir al menos un origen explícito");
        }
        for (String origen : origenes) {
            if (origen.contains("*")) {
                throw new IllegalStateException(
                        "cors.allowed-origins no admite comodines (*) cuando allowCredentials=true: " + origen);
            }
        }
        return origenes;
    }
}