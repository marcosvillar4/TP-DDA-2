package com.example.DA2Back.pedidos.mensajeria;

import static com.example.DA2Back.pedidos.mensajeria.DespachoTopologia.*;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.DA2Back.pedidos.negocio.evento.PedidoListoParaRetirarEvent;

/**
 * Productor: publica en RabbitMQ los pedidos que esperan repartidor.
 *
 * El mensaje se envia AFTER_COMMIT para que el consumidor nunca vea un pedido
 * que todavia no esta confirmado en la base. Si el broker esta caido el pedido
 * queda igualmente LISTO_PARA_RETIRAR (un repartidor puede tomarlo a mano).
 */
@Component
@ConditionalOnProperty(name = "logired.mensajeria.habilitada", havingValue = "true", matchIfMissing = true)
public class PedidoDespachoPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoDespachoPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PedidoDespachoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void alPedidoListoParaRetirar(PedidoListoParaRetirarEvent evento) {
        enviar(evento.pedidoId(), 0, RK_DESPACHO);
    }

    /** Reprograma el despacho: el mensaje espera ESPERA_MS y vuelve a la cola principal. */
    public void reintentar(Long pedidoId, int intentos) {
        enviar(pedidoId, intentos, RK_ESPERA);
    }

    /** Intentos agotados: el mensaje queda en la DLQ para revision manual. */
    public void enviarAFallidos(Long pedidoId, int intentos) {
        enviar(pedidoId, intentos, RK_FALLIDO);
    }

    private void enviar(Long pedidoId, int intentos, String routingKey) {
        try {
            Message mensaje = MessageBuilder
                    .withBody(String.valueOf(pedidoId).getBytes(StandardCharsets.UTF_8))
                    .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
                    .setContentEncoding(StandardCharsets.UTF_8.name())
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .setMessageId(UUID.randomUUID().toString())
                    .setHeader(HEADER_INTENTOS, intentos)
                    .build();
            rabbitTemplate.send(EXCHANGE, routingKey, mensaje);
            log.info("Mensaje publicado: pedido={} rk={} intentos={}", pedidoId, routingKey, intentos);
        } catch (AmqpException e) {
            // No propagamos: la transaccion de negocio ya hizo commit.
            log.error("No se pudo publicar el pedido {} en el broker (rk={}): {}",
                    pedidoId, routingKey, e.getMessage());
        }
    }
}
