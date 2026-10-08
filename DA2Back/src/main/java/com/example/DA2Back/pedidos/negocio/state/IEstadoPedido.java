package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.Repartidor;

public interface IEstadoPedido {
    default void marcarListoParaRetirar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException("No se puede pasar del estado " + pedido.getEstado() + " a LISTO_PARA_RETIRAR");
    }

    default void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        throw new TransicionInvalidaPedidoException("No se puede pasar del estado " + pedido.getEstado() + " a ASIGNADO");
    }

    default void marcarRetirado(Pedido pedido) {
        throw new TransicionInvalidaPedidoException("No se puede pasar del estado " + pedido.getEstado() + " a RETIRADO");
    }

    default void iniciarViaje(Pedido pedido) {
        throw new TransicionInvalidaPedidoException("No se puede pasar del estado " + pedido.getEstado() + " a EN_CAMINO");
    }

    default void entregar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException("No se puede pasar del estado " + pedido.getEstado() + " a ENTREGADO");
    }

    default void cancelar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException("No se puede pasar del estado " + pedido.getEstado() + " a CANCELADO");
    }
}

