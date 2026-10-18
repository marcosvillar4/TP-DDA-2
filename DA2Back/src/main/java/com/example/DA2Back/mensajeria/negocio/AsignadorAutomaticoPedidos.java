package com.example.DA2Back.mensajeria.negocio;

import com.example.DA2Back.mensajeria.config.JmsConfig;
import com.example.DA2Back.mensajeria.excepcion.SinRepartidorDisponibleException;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

/**
 * CONSUMIDOR. Toma de la cola cada pedido LISTO_PARA_RETIRAR y le asigna el primer
 * repartidor disponible, reutilizando ServicioDePedidos.asignarRepartidor (patron State,
 * locks pesimistas y reglas de negocio ya existentes).
 *
 *  - Pedido ya asignado / cancelado / inexistente  -> se descarta el mensaje (idempotente).
 *  - Sin repartidor disponible                     -> excepcion: rollback y redelivery.
 *  - Reintentos agotados                           -> el broker lo mueve a ActiveMQ.DLQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mensajeria.habilitada", havingValue = "true", matchIfMissing = true)
public class AsignadorAutomaticoPedidos {

    private final PedidosRepository pedidosRepository;
    private final RepartidorRepository repartidorRepository;
    private final ServicioDePedidos servicioDePedidos;

    @JmsListener(destination = "${app.mensajeria.cola-pedidos-listos}",
            containerFactory = JmsConfig.FACTORY_PEDIDOS)
    public void alRecibirPedidoListo(String cuerpo) {
        Long pedidoId = Long.valueOf(cuerpo.trim());
        log.info("[MQ] Recibido de la cola: pedido {} listo para retirar", pedidoId);

        Pedido pedido = pedidosRepository.findById(pedidoId).orElse(null);
        if (pedido == null) {
            log.warn("[MQ] Pedido {} inexistente: mensaje descartado", pedidoId);
            return;
        }
        if (pedido.getEstado() != EstadoPedido.LISTO_PARA_RETIRAR || pedido.getRepartidor() != null) {
            log.info("[MQ] Pedido {} ya no esta pendiente de asignacion (estado {}): mensaje descartado",
                    pedidoId, pedido.getEstado());
            return;
        }

        for (Repartidor candidato : repartidorRepository
                .findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor.DISPONIBLE)) {
            try {
                servicioDePedidos.asignarRepartidor(pedidoId, candidato.getId());
                log.info("[MQ] Pedido {} asignado automaticamente al repartidor {}", pedidoId, candidato.getId());
                return;
            } catch (IllegalStateException | TransicionInvalidaPedidoException | NoSuchElementException ex) {
                // Otro proceso lo tomo o tiene un pedido activo: se prueba con el siguiente.
                log.debug("[MQ] Repartidor {} no pudo tomar el pedido {}: {}",
                        candidato.getId(), pedidoId, ex.getMessage());
            }
        }
        throw new SinRepartidorDisponibleException(pedidoId);
    }
}
