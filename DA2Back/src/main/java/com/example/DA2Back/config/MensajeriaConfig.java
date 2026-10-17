package com.example.DA2Back.config;

import static com.example.DA2Back.pedidos.mensajeria.DespachoTopologia.*;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declara en el broker (RabbitMQ) el exchange, las colas y los bindings.
 * RabbitAdmin (autoconfigurado por Spring Boot) los crea al conectarse.
 *
 * Se puede apagar con logired.mensajeria.habilitada=false (ej. tests).
 */
@Configuration
@ConditionalOnProperty(name = "logired.mensajeria.habilitada", havingValue = "true", matchIfMissing = true)
public class MensajeriaConfig {

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    /** Cola principal (punto a punto). Si un mensaje es rechazado va a la DLQ. */
    @Bean
    public Queue colaDespacho() {
        return QueueBuilder.durable(COLA_DESPACHO)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(RK_FALLIDO)
                .build();
    }

    /** Cola de espera: al vencer el TTL el mensaje vuelve a la cola principal. */
    @Bean
    public Queue colaEspera() {
        return QueueBuilder.durable(COLA_ESPERA)
                .ttl(ESPERA_MS)
                .deadLetterExchange(EXCHANGE)
                .deadLetterRoutingKey(RK_DESPACHO)
                .build();
    }

    @Bean
    public Queue colaFallidos() {
        return QueueBuilder.durable(COLA_FALLIDOS).build();
    }

    @Bean
    public Binding bindingDespacho() {
        return BindingBuilder.bind(colaDespacho()).to(pedidosExchange()).with(RK_DESPACHO);
    }

    @Bean
    public Binding bindingEspera() {
        return BindingBuilder.bind(colaEspera()).to(pedidosExchange()).with(RK_ESPERA);
    }

    @Bean
    public Binding bindingFallidos() {
        return BindingBuilder.bind(colaFallidos()).to(pedidosExchange()).with(RK_FALLIDO);
    }
}
