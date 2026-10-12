package com.pedidos360.carrito.config;

import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class RabbitMqConnectionInitializer implements ApplicationRunner {

    private final ConnectionFactory connectionFactory;
    private Connection connection;

    public RabbitMqConnectionInitializer(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void run(ApplicationArguments args) {
        connection = connectionFactory.createConnection();
    }
}