package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

/**
 * Estado ASIGNADO: el pedido tiene un repartidor, que está en camino
 * al punto de retiro. Puede iniciarse el viaje o cancelarse.
 */
@Component
public class EstadoAsignado implements IEstadoPedido {

    @Override
    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        throw new TransicionInvalidaPedidoException(
                "El pedido ya tiene un repartidor asignado (id=" + pedido.getRepartidor().getId() + ")");
    }

    @Override
    public void iniciarViaje(Pedido pedido) {
        pedido.setEstado(EstadoPedido.EN_CAMINO);
    }

    @Override
    public void entregar(Pedido pedido) {
        throw new TransicionInvalidaPedidoException(
                "Un pedido ASIGNADO no puede marcarse como entregado sin iniciar el viaje primero");
    }

    @Override
    public void cancelar(Pedido pedido) {
        // Al cancelar con repartidor ya asignado, el repartidor queda disponible.
        // La lógica de liberar al repartidor se maneja en el servicio,
        // ya que este estado solo muta el pedido.
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
