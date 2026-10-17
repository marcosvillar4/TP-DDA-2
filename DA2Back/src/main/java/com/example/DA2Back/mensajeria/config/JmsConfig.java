package com.example.DA2Back.mensajeria.config;

import jakarta.jms.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;

/**
 * Configuracion JMS de los consumidores. La ConnectionFactory (ActiveMQ) la crea Spring Boot
 * a partir de spring.activemq.broker-url.
 */
@Configuration
@EnableJms
public class JmsConfig {

    public static final String FACTORY_PEDIDOS = "pedidosListenerFactory";

    /**
     * Sesion transaccional: si el listener lanza una excepcion el mensaje NO se confirma,
     * se hace rollback y el broker lo reentrega (politica de redelivery configurada en la
     * URL del broker). Un solo consumidor concurrente para asignar de a un pedido por vez.
     */
    @Bean(name = FACTORY_PEDIDOS)
    public DefaultJmsListenerContainerFactory pedidosListenerFactory(ConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        factory.setConcurrency("1");
        return factory;
    }
}
