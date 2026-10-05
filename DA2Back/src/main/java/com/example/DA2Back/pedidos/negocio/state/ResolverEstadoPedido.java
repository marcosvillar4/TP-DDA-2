package com.example.DA2Back.pedidos.negocio.state;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resuelve qué implementación de IEstadoPedido corresponde
 * al enum EstadoPedido almacenado en la base de datos.
 *
 * Actúa como puente entre la capa de persistencia (enum) y
 * la capa de comportamiento (beans de Spring), con la misma
 * filosofía que ResolverEstadoUsuario en el módulo de Seguridad.
 */
@Component
@RequiredArgsConstructor
public class ResolverEstadoPedido {

    private final EstadoPendienteCotizacion estadoPendienteCotizacion;
    private final EstadoAsignado estadoAsignado;
    private final EstadoEnCamino estadoEnCamino;
    private final EstadoEntregado estadoEntregado;
    private final EstadoCancelado estadoCancelado;

    public IEstadoPedido resolver(EstadoPedido estado) {
        return switch (estado) {
            case PENDIENTE_COTIZACION -> estadoPendienteCotizacion;
            case ASIGNADO             -> estadoAsignado;
            case EN_CAMINO            -> estadoEnCamino;
            case ENTREGADO            -> estadoEntregado;
            case CANCELADO            -> estadoCancelado;
        };
    }
}
