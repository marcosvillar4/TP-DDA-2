package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

/**
 * Estado EN_CAMINO: el repartidor ya retiró el paquete y está
 * en ruta hacia el destino. Solo puede entregarse o cancelarse.
 */
@Component
public class EstadoEnCamino implements IEstadoPedido {

    @Override
    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        throw new TransicionInvalidaPedidoException(
                "No se puede reasignar un pedido que ya está en camino");
    }

    @Override
    public void iniciarViaje(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "El pedido ya está EN_CAMINO");
    }

    @Override
    public void entregar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.ENTREGADO);
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
