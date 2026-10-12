package com.pedidos360.rabbitadmin.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de mensajeria del microservicio administrador.
 *
 * RabbitAdmin es la clase de Spring AMQP que permite declarar y eliminar
 * colas, exchanges y bindings en tiempo de ejecucion. Solo la usa
 * {@link com.pedidos360.rabbitadmin.service.RabbitAdminService}: el controlador
 * nunca habla directamente con RabbitMQ.
 */
@Configuration
public class RabbitAdminConfig {

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
