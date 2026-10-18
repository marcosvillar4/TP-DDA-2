package com.example.DA2Back.mensajeria.excepcion;

/**
 * Se lanza desde el consumidor cuando no hay ningun repartidor que pueda tomar el pedido.
 * Al propagarse, la sesion JMS (transaccional) hace rollback y el broker reentrega el
 * mensaje segun la politica de redelivery. Agotados los reintentos, el mensaje pasa a la DLQ.
 */
public class SinRepartidorDisponibleException extends RuntimeException {
    public SinRepartidorDisponibleException(Long pedidoId) {
        super("No hay repartidores disponibles para el pedido " + pedidoId);
    }
}
