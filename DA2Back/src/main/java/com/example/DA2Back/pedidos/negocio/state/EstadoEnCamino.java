package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import org.springframework.stereotype.Component;

@Component
public class EstadoEnCamino implements IEstadoPedido {
    @Override
    public void entregar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.ENTREGADO);
    }
}
