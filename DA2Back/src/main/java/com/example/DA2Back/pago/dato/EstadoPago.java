package com.example.DA2Back.pago.dato;

/**
 * Ciclo de vida de un pago de envio.
 * PENDIENTE -> PAGADO (el comercio abono) o PENDIENTE -> ANULADO (el pedido se cancelo).
 */
public enum EstadoPago {
    PENDIENTE,
    PAGADO,
    ANULADO
}
