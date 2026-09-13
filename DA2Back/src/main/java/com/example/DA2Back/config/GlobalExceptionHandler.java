package com.example.DA2Back.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.DA2Back.Seguridad.excepcion.*;

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

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    /**
     * DataIntegrityViolationException → 500 Internal Server Error.
     * Evita que errores de persistencia no manejados terminen delegando en /error
     * y se oculten como 403 por la configuración de seguridad.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(
            DataIntegrityViolationException ex) {

        log.warn("Error de integridad al persistir datos. Causa: {}", obtenerCausaRaiz(ex));

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo guardar el registro por una inconsistencia de integridad en la base de datos"
        );
    }

    // -------------------------------------------------------------------------
    /*
     * Excepciones de Seguridad
     */

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(UsuarioYaExisteException.class)
    public ResponseEntity<Map<String, String>> handleYaExiste(UsuarioYaExisteException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TransicionInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleTransicionInvalida(TransicionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> handleCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", ex.getMessage()));
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

    private String obtenerCausaRaiz(Throwable ex) {

        Throwable actual = ex;

        while (actual.getCause() != null) {
            actual = actual.getCause();
        }

        return actual.getMessage() != null
                ? actual.getMessage()
                : actual.getClass().getSimpleName();
    }
}
