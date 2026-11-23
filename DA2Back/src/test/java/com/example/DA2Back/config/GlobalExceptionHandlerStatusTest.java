package com.example.DA2Back.config;

import com.example.DA2Back.Seguridad.excepcion.CredencialesInvalidasException;
import com.example.DA2Back.Seguridad.excepcion.RefreshTokenInvalidoException;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;
import com.example.DA2Back.Seguridad.excepcion.UsuarioBloqueadoException;
import com.example.DA2Back.Seguridad.excepcion.UsuarioYaExisteException;
import com.example.DA2Back.deposito.excepcion.AsociacionInvalidaException;
import com.example.DA2Back.pago.excepcion.PagoRechazadoException;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Contrato de codigos HTTP del manejador global de errores (criterio de Eje B/C:
 * 400/401/403/404/409/422 correctos y sin filtrar detalles internos).
 */
class GlobalExceptionHandlerStatusTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Transicion invalida de PEDIDO -> 409 Conflict con el mensaje de la regla")
    void transicionPedido_409() {
        ResponseEntity<Map<String, Object>> r =
                handler.handleTransicionInvalidaPedido(new TransicionInvalidaPedidoException("no se puede"));

        assertEquals(HttpStatus.CONFLICT, r.getStatusCode());
        assertEquals(409, r.getBody().get("status"));
        assertEquals("Conflict", r.getBody().get("error"));
        assertEquals("no se puede", r.getBody().get("message"));
        assertNotNull(r.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Estado invalido de la logica de negocio (IllegalState) -> 409; pago rechazado -> 422")
    void illegalStateYPago() {
        assertEquals(HttpStatus.CONFLICT, handler.handleIllegalState(new IllegalStateException("x")).getStatusCode());
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY,
                handler.handlePagoRechazado(new PagoRechazadoException("sin fondos")).getStatusCode());
    }

    @Test
    @DisplayName("Dato invalido -> 400; sin detalle -> mensaje por defecto, nunca null")
    void illegalArgument_400() {
        assertEquals(HttpStatus.BAD_REQUEST, handler.handleIllegalArgument(new IllegalArgumentException("dato")).getStatusCode());
        ResponseEntity<Map<String, Object>> sinMensaje = handler.handleIllegalArgument(new IllegalArgumentException());
        assertEquals("Sin detalle", sinMensaje.getBody().get("message"));
    }

    @Test
    @DisplayName("Validacion de bean -> 400 con 'campo: mensaje'; sin errores -> 'Datos invalidos'")
    void validacionBean_400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult binding = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(binding);
        when(binding.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "email", "debe ser valido"),
                new FieldError("dto", "monto", "debe ser mayor a cero")));

        ResponseEntity<Map<String, Object>> r = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        String mensaje = (String) r.getBody().get("message");
        assertTrue(mensaje.contains("email: debe ser valido"));
        assertTrue(mensaje.contains("monto: debe ser mayor a cero"));

        when(binding.getFieldErrors()).thenReturn(List.of());
        assertEquals("Datos inválidos", handler.handleValidation(ex).getBody().get("message"));
    }

    @Test
    @DisplayName("No encontrado (todas las variantes) -> 404; acceso denegado -> 403")
    void noEncontradoYAcceso() {
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNotFound(new NoSuchElementException("x")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNoEncontrado(new RecursoNoEncontradoException("x")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, handler.handleNoEncontradoComercioDeposito(
                new com.example.DA2Back.comercio.excepcion.RecursoNoEncontradoException("x")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, handler.handleAccessDenied(new AccessDeniedException("x")).getStatusCode());
    }

    @Test
    @DisplayName("Conflictos de asociacion y usuario duplicado -> 409")
    void conflictos() {
        assertEquals(HttpStatus.CONFLICT, handler.handleAsociacionInvalida(new AsociacionInvalidaException("x")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT, handler.handleYaExiste(new UsuarioYaExisteException("x")).getStatusCode());
    }

    @Test
    @DisplayName("Usuario bloqueado -> 403; credenciales o refresh token invalidos -> 401")
    void autenticacion() {
        ResponseEntity<Map<String, String>> bloqueado = handler.handleBloqueado(new UsuarioBloqueadoException("bloqueado"));
        assertEquals(HttpStatus.FORBIDDEN, bloqueado.getStatusCode());
        assertEquals("bloqueado", bloqueado.getBody().get("message"));

        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.handleCredencialesInvalidas(new CredencialesInvalidasException("x")).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED,
                handler.handleCredencialesInvalidas(new RefreshTokenInvalidoException("x")).getStatusCode());
    }

    @Test
    @DisplayName("Transicion invalida de USUARIO -> 400 (contrato actual; el de Pedido es 409)")
    void transicionUsuario_400() {
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handleTransicionInvalida(new TransicionInvalidaException("x")).getStatusCode());
    }
}
