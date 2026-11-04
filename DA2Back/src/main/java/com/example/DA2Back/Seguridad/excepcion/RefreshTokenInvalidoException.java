package com.example.DA2Back.Seguridad.excepcion;

/**
 * El refresh token no existe, está revocado (logout / rotación) o venció.
 * Se traduce a HTTP 401 Unauthorized en GlobalExceptionHandler.
 */
public class RefreshTokenInvalidoException extends RuntimeException {

    public RefreshTokenInvalidoException(String message) {
        super(message);
    }
}
