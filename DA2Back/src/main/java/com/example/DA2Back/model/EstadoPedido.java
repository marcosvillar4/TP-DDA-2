package com.example.DA2Back.model;

/**
 * Estados posibles del ciclo de vida de un Pedido
 * en el sistema de logistica de ultima milla.
 */
public enum EstadoPedido {
    CREADO,
    ASIGNADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO
}