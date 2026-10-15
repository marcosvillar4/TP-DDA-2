package com.example.DA2Back.pedidos.dato;

/** Estados auditados, incluidos los que ya no pertenecen al ciclo operativo. */
public enum EstadoHistoricoPedido {
    CREADO,
    LISTO_PARA_RETIRAR,
    ASIGNADO,
    RETIRADO,
    EN_CAMINO,
    ENTREGADO,
    CANCELADO,
    PENDIENTE_COTIZACION;

    public static EstadoHistoricoPedido desdeOperativo(EstadoPedido estado) {
        return valueOf(estado.name());
    }
}
