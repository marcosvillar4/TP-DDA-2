package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

/**
 * Estado CANCELADO: estado final del ciclo de vida.
 * El pedido fue cancelado. No admite ninguna transición.
 */
@Component
public class EstadoCancelado implements IEstadoPedido {

    @Override
    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        throw new TransicionInvalidaPedidoException(
                "El pedido está cancelado y no admite modificaciones");
    }

    @Override
    public void iniciarViaje(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "El pedido está cancelado y no admite modificaciones");
    }

    @Override
    public void entregar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "El pedido está cancelado y no admite modificaciones");
    }

    @Override
    public void cancelar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "El pedido ya está cancelado");
    }
}
