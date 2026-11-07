package com.example.DA2Back.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * SEC-W02: responde 401 Unauthorized cuando se accede a un recurso protegido
 * sin una autenticacion valida: sin token, token vencido, alterado o firmado
 * con alg "none". (Por defecto Spring Security respondia 403 en estos casos.)
 *
 * El cuerpo mantiene el formato de errores de GlobalExceptionHandler.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String MSJ_VENCIDO = "Token de acceso vencido";
    static final String MSJ_INVALIDO = "Token de acceso inválido";
    static final String MSJ_AUSENTE = "Se requiere autenticación";

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        Object motivo = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_MOTIVO_JWT);
        String mensaje;
        if (JwtAuthenticationFilter.MOTIVO_VENCIDO.equals(motivo)) {
            mensaje = MSJ_VENCIDO;
        } else if (JwtAuthenticationFilter.MOTIVO_INVALIDO.equals(motivo)) {
            mensaje = MSJ_INVALIDO;
        } else {
            mensaje = MSJ_AUSENTE;
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":401,"
                + "\"error\":\"Unauthorized\","
                + "\"message\":\"" + mensaje + "\"}");
    }
}
