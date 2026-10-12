package com.pedidos360.carrito.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;

import static org.assertj.core.api.Assertions.assertThat;

/** Comprueba que los beans de RabbitMQ se arman con los nombres y politicas de application.yml. */
class RabbitMQConfigTest {

    private final RabbitMQProperties props = new RabbitMQProperties(
            new RabbitMQProperties.Exchanges("pedidos.exchange", "pedidos.dlx"),
            new RabbitMQProperties.RoutingKeys("pedido.creado"),
            new RabbitMQProperties.Colas(
                    new RabbitMQProperties.ColaConDlq("pedidos.ticket.queue", "pedidos.ticket.dlq", "pedidos.ticket.dead")),
            new RabbitMQProperties.Retencion(60000, 1000, 86400000),
            new RabbitMQProperties.Reintentos(3, 1000, 2.0));

    private final RabbitMQConfig config = new RabbitMQConfig(props);

    @Test
    void laColaDeTicketsEsDurableYEnviaSusMensajesFallidosALaDlx() {
        Queue cola = config.ticketQueue();

        assertThat(cola.getName()).isEqualTo("pedidos.ticket.queue");
        assertThat(cola.isDurable()).isTrue();
        assertThat(cola.getArguments())
                .containsEntry("x-dead-letter-exchange", "pedidos.dlx")
                .containsEntry("x-dead-letter-routing-key", "pedidos.ticket.dead")
                .containsEntry("x-message-ttl", 60000)
                .containsEntry("x-max-length", 1000);
    }

    @Test
    void losBindingsUnenCadaColaConSuExchangeYRoutingKey() {
        Binding ticket = config.ticketBinding();
        assertThat(ticket.getExchange()).isEqualTo("pedidos.exchange");
        assertThat(ticket.getDestination()).isEqualTo("pedidos.ticket.queue");
        assertThat(ticket.getRoutingKey()).isEqualTo("pedido.creado");

        Binding dlq = config.ticketDlqBinding();
        assertThat(dlq.getExchange()).isEqualTo("pedidos.dlx");
        assertThat(dlq.getDestination()).isEqualTo("pedidos.ticket.dlq");
        assertThat(dlq.getRoutingKey()).isEqualTo("pedidos.ticket.dead");
    }
}
