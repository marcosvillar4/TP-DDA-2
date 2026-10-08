package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResolverEstadoPedido {

    private final EstadoCreado estadoCreado;
    private final EstadoListoParaRetirar estadoListoParaRetirar;
    private final EstadoAsignado estadoAsignado;
    private final EstadoRetirado estadoRetirado;
    private final EstadoEnCamino estadoEnCamino;
    private final EstadoEntregado estadoEntregado;
    private final EstadoCancelado estadoCancelado;

    public IEstadoPedido resolver(EstadoPedido estado) {
        return switch (estado) {
            case CREADO -> estadoCreado;
            case LISTO_PARA_RETIRAR -> estadoListoParaRetirar;
            case ASIGNADO -> estadoAsignado;
            case RETIRADO -> estadoRetirado;
            case EN_CAMINO -> estadoEnCamino;
            case ENTREGADO -> estadoEntregado;
            case CANCELADO -> estadoCancelado;
        };
    }
}
