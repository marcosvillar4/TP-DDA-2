package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.springframework.stereotype.Component;

@Component
public class EstadoListoParaRetirar implements IEstadoPedido {
    @Override
    public void asignarRepartidor(Pedido pedido, Repartidor repartidor) {
        pedido.setRepartidor(repartidor);
        pedido.setEstado(EstadoPedido.ASIGNADO);
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
