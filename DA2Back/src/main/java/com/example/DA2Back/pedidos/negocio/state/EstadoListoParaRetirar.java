package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

@Component
public class EstadoListoParaRetirar implements IEstadoPedido {
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
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
