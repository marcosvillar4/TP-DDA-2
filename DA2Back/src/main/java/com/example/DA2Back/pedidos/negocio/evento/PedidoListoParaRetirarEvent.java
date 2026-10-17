package com.example.DA2Back.pedidos.negocio.evento;

/**
 * Evento de dominio (interno a la JVM) que ServicioDePedidosImpl publica cuando un
 * pedido pasa a LISTO_PARA_RETIRAR. El modulo de mensajeria lo escucha y, una vez
 * confirmada la transaccion, lo traduce a un mensaje en la cola de ActiveMQ.
 *
 * Asi el modulo de pedidos no conoce JMS ni el broker.
 */
public record PedidoListoParaRetirarEvent(Long pedidoId) {
}
