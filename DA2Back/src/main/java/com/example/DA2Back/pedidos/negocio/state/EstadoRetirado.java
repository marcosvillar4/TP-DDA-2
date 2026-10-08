package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import org.springframework.stereotype.Component;

@Component
public class EstadoRetirado implements IEstadoPedido {
    @Override
    public void iniciarViaje(Pedido pedido) {
        pedido.setEstado(EstadoPedido.EN_CAMINO);
    }
    
    @Override
    public void cancelar(Pedido pedido) {
        pedido.setEstado(EstadoPedido.CANCELADO);
    }
}
