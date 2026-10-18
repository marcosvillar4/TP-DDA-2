package com.example.DA2Back.mensajeria.negocio;

import com.example.DA2Back.pedidos.negocio.evento.PedidoListoParaRetirarEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * PRODUCTOR. Publica en la cola punto a punto un mensaje por cada pedido que queda
 * LISTO_PARA_RETIRAR.
 *
 * Se envia recien DESPUES del commit de la transaccion del pedido: de esa forma el
 * consumidor nunca recibe un mensaje de un pedido que todavia no es visible en la base.
 * Si el envio falla, el pedido queda LISTO_PARA_RETIRAR y sigue pudiendo asignarse
 * manualmente (admin) o ser tomado por un repartidor.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.mensajeria.habilitada", havingValue = "true", matchIfMissing = true)
public class PublicadorPedidosListos {

    private final JmsTemplate jmsTemplate;
    private final String cola;

    public PublicadorPedidosListos(JmsTemplate jmsTemplate,
                                   @Value("${app.mensajeria.cola-pedidos-listos}") String cola) {
        this.jmsTemplate = jmsTemplate;
        this.cola = cola;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alConfirmarseElPedido(PedidoListoParaRetirarEvent evento) {
        Long pedidoId = evento.pedidoId();
        try {
            jmsTemplate.send(cola, session -> {
                var mensaje = session.createTextMessage(String.valueOf(pedidoId));
                mensaje.setLongProperty("pedidoId", pedidoId);
                mensaje.setStringProperty("tipoEvento", "PEDIDO_LISTO_PARA_RETIRAR");
                return mensaje;
            });
            log.info("[MQ] Publicado en la cola '{}': pedido {} listo para retirar", cola, pedidoId);
        } catch (JmsException ex) {
            log.error("[MQ] No se pudo publicar el pedido {} en la cola '{}'. Queda LISTO_PARA_RETIRAR "
                    + "para asignacion manual", pedidoId, cola, ex);
        }
    }
}
