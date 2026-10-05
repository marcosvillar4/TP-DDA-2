package com.example.DA2Back.pedidos.excepcion;

/**
 * Se lanza cuando se intenta realizar una transición de estado
 * inválida sobre un Pedido (ej: pasar de ENTREGADO a CREADO).
 */
public class TransicionInvalidaPedidoException extends RuntimeException {
    public TransicionInvalidaPedidoException(String message) {
        super(message);
    }
}
