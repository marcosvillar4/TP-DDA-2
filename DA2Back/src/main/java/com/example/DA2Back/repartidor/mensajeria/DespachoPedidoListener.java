package com.example.DA2Back.repartidor.mensajeria;

import static com.example.DA2Back.pedidos.mensajeria.DespachoTopologia.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.pedidos.mensajeria.PedidoDespachoPublisher;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;

/**
 * Consumidor: toma de la cola "pedidos.despacho" y asigna un repartidor
 * disponible al pedido (LISTO_PARA_RETIRAR -> ASIGNADO).
 *
 * Es idempotente: si el pedido ya fue asignado o cancelado, descarta el mensaje.
 * Si no hay repartidores libres, lo reprograma en la cola de espera; tras
 * MAX_INTENTOS va a la DLQ. Una excepcion inesperada rechaza el mensaje sin
 * re-encolar (default-requeue-rejected=false) y RabbitMQ lo manda a la DLQ.
 */
@Component
@ConditionalOnProperty(name = "logired.mensajeria.habilitada", havingValue = "true", matchIfMissing = true)
public class DespachoPedidoListener {

    private static final Logger log = LoggerFactory.getLogger(DespachoPedidoListener.class);

    private final PedidosRepository pedidosRepository;
    private final RepartidorRepository repartidorRepository;
    private final ServicioDePedidos servicioDePedidos;
    private final PedidoDespachoPublisher publisher;

    public DespachoPedidoListener(PedidosRepository pedidosRepository,
                                  RepartidorRepository repartidorRepository,
                                  ServicioDePedidos servicioDePedidos,
                                  PedidoDespachoPublisher publisher) {
        this.pedidosRepository = pedidosRepository;
        this.repartidorRepository = repartidorRepository;
        this.servicioDePedidos = servicioDePedidos;
        this.publisher = publisher;
    }

    @RabbitListener(queues = COLA_DESPACHO)
    public void onMensaje(Message mensaje) {
        Long pedidoId = Long.valueOf(new String(mensaje.getBody(), StandardCharsets.UTF_8).trim());
        Object header = mensaje.getMessageProperties().getHeader(HEADER_INTENTOS);
        int intentos = header instanceof Number n ? n.intValue() : 0;
        procesar(pedidoId, intentos);
    }

    /** Logica de despacho (separada del transporte para poder testearla). */
    public void procesar(Long pedidoId, int intentos) {
        if (!esAsignable(pedidoId)) {
            log.info("Pedido {} ya no esta pendiente de asignacion; mensaje descartado", pedidoId);
            return;
        }

        List<Repartidor> candidatos =
                repartidorRepository.findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor.DISPONIBLE);

        for (Repartidor candidato : candidatos) {
            try {
                servicioDePedidos.asignarRepartidor(pedidoId, candidato.getId());
                log.info("Pedido {} asignado al repartidor {} via mensajeria", pedidoId, candidato.getId());
                return;
            } catch (IllegalStateException | TransicionInvalidaPedidoException e) {
                // Otro consumidor/operacion le gano el repartidor o el pedido: probamos el siguiente.
                log.debug("No se pudo asignar pedido {} a repartidor {}: {}",
                        pedidoId, candidato.getId(), e.getMessage());
                if (!esAsignable(pedidoId)) {
                    return;
                }
            }
        }

        int proximo = intentos + 1;
        if (proximo < MAX_INTENTOS) {
            log.info("Sin repartidores disponibles para el pedido {} (intento {}/{}); se reintenta en {} ms",
                    pedidoId, proximo, MAX_INTENTOS, ESPERA_MS);
            publisher.reintentar(pedidoId, proximo);
        } else {
            log.warn("Pedido {} sin repartidor tras {} intentos; se envia a la DLQ", pedidoId, proximo);
            publisher.enviarAFallidos(pedidoId, proximo);
        }
    }

    private boolean esAsignable(Long pedidoId) {
        return pedidosRepository.findById(pedidoId)
                .map(this::esAsignable)
                .orElse(false);
    }

    private boolean esAsignable(Pedido pedido) {
        return pedido.getEstado() == EstadoPedido.LISTO_PARA_RETIRAR && pedido.getRepartidor() == null;
    }
}
