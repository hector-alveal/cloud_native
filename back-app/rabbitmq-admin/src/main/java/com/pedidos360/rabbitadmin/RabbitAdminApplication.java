package com.pedidos360.rabbitadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio administrador de RabbitMQ.
 * Expone una API REST para crear y eliminar colas, exchanges y bindings
 * sin tener que entrar al dashboard de RabbitMQ.
 */
@SpringBootApplication
public class RabbitAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitAdminApplication.class, args);
    }
}
