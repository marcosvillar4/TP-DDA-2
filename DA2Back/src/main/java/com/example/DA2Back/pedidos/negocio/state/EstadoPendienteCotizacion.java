package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

/**
 * Estado PENDIENTE_COTIZACION: el pedido fue generado por el comercio pero
 * todavía no tiene repartidor asignado. Es el único estado desde el que se
 * puede asignar un repartidor.
 */
@Component
public class EstadoPendienteCotizacion implements IEstadoPedido {

    @Override
    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        if (!repartidor.isActivo()) {
            throw new TransicionInvalidaPedidoException(
                    "No se puede asignar el pedido a un repartidor inactivo");
        }
        if (repartidor.getEstado() != EstadoRepartidor.DISPONIBLE) {
            throw new TransicionInvalidaPedidoException(
                    "El repartidor no está disponible (estado actual: " + repartidor.getEstado() + ")");
        }
        pedido.setRepartidor(repartidor);
        pedido.setEstado(EstadoPedido.ASIGNADO);
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);
    }

    @Override
    public void iniciarViaje(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "Un pedido PENDIENTE_COTIZACION no puede iniciar viaje sin tener un repartidor asignado primero");
    }

    @Override
    public void entregar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "Un pedido PENDIENTE_COTIZACION no puede marcarse como entregado");
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
