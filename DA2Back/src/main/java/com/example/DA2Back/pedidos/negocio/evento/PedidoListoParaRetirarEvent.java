package com.example.DA2Back.pedidos.negocio.evento;

/**
 * Evento de dominio (interno a Spring) que se emite cuando un pedido pasa a
 * LISTO_PARA_RETIRAR. Un listener transaccional lo traduce a un mensaje
 * AMQP recien despues del commit.
 */
public record PedidoListoParaRetirarEvent(Long pedidoId) {
}
