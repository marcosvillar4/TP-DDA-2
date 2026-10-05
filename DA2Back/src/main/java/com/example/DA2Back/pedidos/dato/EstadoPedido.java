package com.example.DA2Back.pedidos.dato;

/**
 * Estados posibles del ciclo de vida de un Pedido
 * en el sistema de logistica de ultima milla.
 */
public enum EstadoPedido {
    PENDIENTE_COTIZACION,
    ASIGNADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}