package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

/**
 * Estado ENTREGADO: estado final del ciclo de vida.
 * El pedido llegó al destinatario. No admite ninguna transición.
 */
@Component
public class EstadoEntregado implements IEstadoPedido {

    @Override
    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        throw new TransicionInvalidaPedidoException(
                "El pedido ya fue entregado y no admite modificaciones");
    }

    @Override
    public void iniciarViaje(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "El pedido ya fue entregado y no admite modificaciones");
    }

    @Override
    public void entregar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "El pedido ya fue entregado");
    }

    @Override
    public void cancelar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "No se puede cancelar un pedido que ya fue entregado");
    }
}
