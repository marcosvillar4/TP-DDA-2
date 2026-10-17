package com.example.DA2Back.pedidos.mensajeria;

/**
 * Nombres de la topologia RabbitMQ del proceso "despacho de pedidos".
 *
 * <pre>
 *  Comercio/Deposito --(LISTO_PARA_RETIRAR)--> [exchange logired.pedidos]
 *      rk "despacho"  -> cola pedidos.despacho        (punto a punto, 1 consumidor gana)
 *      rk "espera"    -> cola pedidos.despacho.espera (TTL; al vencer vuelve a "despacho")
 *      rk "fallido"   -> cola pedidos.despacho.dlq    (mensajes agotados / venenosos)
 * </pre>
 */
public final class DespachoTopologia {

    public static final String EXCHANGE = "logired.pedidos";

    public static final String COLA_DESPACHO = "pedidos.despacho";
    public static final String COLA_ESPERA = "pedidos.despacho.espera";
    public static final String COLA_FALLIDOS = "pedidos.despacho.dlq";

    public static final String RK_DESPACHO = "despacho";
    public static final String RK_ESPERA = "espera";
    public static final String RK_FALLIDO = "fallido";

    public static final String HEADER_INTENTOS = "intentos";

    /** Tiempo que un pedido sin repartidor espera antes de reintentar el despacho. */
    public static final int ESPERA_MS = 15_000;
    /** Intentos totales antes de enviar el mensaje a la DLQ. */
    public static final int MAX_INTENTOS = 8;

    private DespachoTopologia() {
    }
}
