package com.example.DA2Back.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones de la capa de presentacion.
 *
 * Mapea excepciones de negocio a respuestas HTTP estandarizadas,
 * evitando que los errores se propaguen como stack traces al cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Falla de @Valid en el body (ej. registro con campos faltantes) → 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return buildError(
                HttpStatus.BAD_REQUEST,
                mensaje.isBlank() ? "Datos inválidos" : mensaje
        );
    }

    /**
     * IllegalArgumentException → 400 Bad Request
     * Ejemplo: email duplicado al registrar un comercio.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * IllegalStateException → 409 Conflict
     * Ejemplo: intentar cancelar un pedido ya entregado.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex) {

        return buildError(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * NoSuchElementException → 404 Not Found
     * Ejemplo: pedido o comercio inexistente.
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoSuchElementException ex) {

        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // -------------------------------------------------------------------------

    private ResponseEntity<Map<String, Object>> buildError(
            HttpStatus status, String message) {

        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status",    status.value(),
                "error",     status.getReasonPhrase(),
                "message",   message != null ? message : "Sin detalle"
        ));
    }
}