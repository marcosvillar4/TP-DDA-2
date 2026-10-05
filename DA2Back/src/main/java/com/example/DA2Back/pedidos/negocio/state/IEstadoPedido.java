package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.repartidor.dato.Repartidor;

/**
 * Contrato del patrón State para el ciclo de vida de un Pedido.
 *
 * Cada implementación define qué transiciones son válidas desde su estado.
 * Las transiciones que no aplican lanzan TransicionInvalidaPedidoException,
 * evitando la proliferación de if/switch en el servicio.
 *
 * Mapa de transiciones válidas:
 *   CREADO    → asignarRepartidor() → ASIGNADO
 *   CREADO    → cancelar()          → CANCELADO
 *   ASIGNADO  → iniciarViaje()      → EN_CAMINO
 *   ASIGNADO  → cancelar()          → CANCELADO
 *   EN_CAMINO → entregar()          → ENTREGADO
 *   EN_CAMINO → cancelar()          → CANCELADO
 *   ENTREGADO → [estado final, ninguna transición permitida]
 *   CANCELADO → [estado final, ninguna transición permitida]
 */
public interface IEstadoPedido {
    void asignarRepartidor(Pedido pedido, Repartidor repartidor);
    void iniciarViaje(Pedido pedido);
    void entregar(Pedido pedido);
    void cancelar(Pedido pedido);
}
