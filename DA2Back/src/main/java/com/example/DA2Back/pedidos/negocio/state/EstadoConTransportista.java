package com.example.DA2Back.pedidos.negocio.state;

import org.springframework.stereotype.Component;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;

@Component
public class EstadoConTransportista implements IEstadoPedido {

    @Override
    public void marcarEnviado(Pedido pedido) {
        pedido.setEstado(EstadoPedido.ENTREGADO);
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
