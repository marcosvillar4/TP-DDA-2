package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import org.springframework.stereotype.Component;

@Component
public class EstadoCreado implements IEstadoPedido {
    @Override
    public void marcarListoParaRetirar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.LISTO_PARA_RETIRAR);
    }

    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
