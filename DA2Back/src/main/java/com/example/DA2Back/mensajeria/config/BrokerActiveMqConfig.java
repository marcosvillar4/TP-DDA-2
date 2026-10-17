package com.example.DA2Back.mensajeria.config;

import org.apache.activemq.broker.BrokerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Levanta un broker ActiveMQ embebido dentro de la propia aplicacion, expuesto por TCP.
 * Permite correr el TP sin instalar nada ni usar Docker.
 *
 * Es un broker "de verdad": escucha en tcp://localhost:61616 y el productor y el
 * consumidor se conectan a el por red, igual que con un ActiveMQ standalone.
 * Para usar un ActiveMQ instalado aparte: app.mensajeria.broker-embebido=false
 */
@Configuration
@ConditionalOnProperty(name = "app.mensajeria.broker-embebido", havingValue = "true")
public class BrokerActiveMqConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService brokerService(
            @Value("${app.mensajeria.broker-url-escucha:tcp://localhost:61616}") String urlEscucha)
            throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("logired-broker");
        broker.setPersistent(false);      // mensajes en memoria: no requiere store en disco
        broker.setUseJmx(false);
        broker.setUseShutdownHook(false); // el ciclo de vida lo maneja Spring
        broker.addConnector(urlEscucha);
        return broker;
    }
}
