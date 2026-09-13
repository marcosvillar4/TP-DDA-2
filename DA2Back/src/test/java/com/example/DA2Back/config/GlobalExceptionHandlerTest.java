package com.example.DA2Back.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void dataIntegrityViolation_devuelveErrorInternoControlado() {
        DataIntegrityViolationException ex =
                new DataIntegrityViolationException("Violación de integridad de datos");

        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrity(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals(
                "No se pudo guardar el registro por una inconsistencia de integridad en la base de datos",
                response.getBody().get("message")
        );
    }
}
