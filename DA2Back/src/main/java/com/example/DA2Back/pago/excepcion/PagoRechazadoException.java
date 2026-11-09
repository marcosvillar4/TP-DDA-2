package com.example.DA2Back.pago.excepcion;

/**
 * La pasarela (simulada) rechazo la operacion: fondos insuficientes, tarjeta
 * rechazada por el emisor, etc. Se responde 422; el pago queda PENDIENTE.
 */
public class PagoRechazadoException extends RuntimeException {

    public PagoRechazadoException(String message) {
        super(message);
    }
}
