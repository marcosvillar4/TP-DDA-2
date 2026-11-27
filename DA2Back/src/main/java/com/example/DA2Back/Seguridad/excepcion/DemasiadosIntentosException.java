package com.example.DA2Back.Seguridad.excepcion;

/**
 * Excepción lanzada cuando se alcanza el límite de intentos fallidos de login (B3 / SEC-W01).
 * El manejador global la traduce a HTTP 429 Too Many Requests con la cabecera Retry-After.
 */
public class DemasiadosIntentosException extends RuntimeException {

    private final long retryAfterSeconds;

    public DemasiadosIntentosException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds > 0 ? retryAfterSeconds : 900L;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
