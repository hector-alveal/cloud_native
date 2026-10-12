package com.pedidos360.catalogo.config;

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
                    new RabbitMQProperties.ColaConDlq("pedidos.stock.queue", "pedidos.stock.dlq", "pedidos.stock.dead")),
            new RabbitMQProperties.Retencion(60000, 1000, 86400000),
            new RabbitMQProperties.Reintentos(3, 1000, 2.0));

    private final RabbitMQConfig config = new RabbitMQConfig(props);

    @Test
    void laColaDeStockEsDurableYEnviaSusMensajesFallidosALaDlx() {
        Queue cola = config.stockQueue();

        assertThat(cola.getName()).isEqualTo("pedidos.stock.queue");
        assertThat(cola.isDurable()).isTrue();
        assertThat(cola.getArguments())
                .containsEntry("x-dead-letter-exchange", "pedidos.dlx")
                .containsEntry("x-dead-letter-routing-key", "pedidos.stock.dead")
                .containsEntry("x-message-ttl", 60000)
                .containsEntry("x-max-length", 1000);
    }

    @Test
    void laDlqRetieneLosMensajes24Horas() {
        Queue dlq = config.stockDlq();

        assertThat(dlq.getName()).isEqualTo("pedidos.stock.dlq");
        assertThat(dlq.getArguments()).containsEntry("x-message-ttl", 86400000);
    }

    @Test
    void losBindingsUnenCadaColaConSuExchangeYRoutingKey() {
        Binding stock = config.stockBinding();
        assertThat(stock.getExchange()).isEqualTo("pedidos.exchange");
        assertThat(stock.getDestination()).isEqualTo("pedidos.stock.queue");
        assertThat(stock.getRoutingKey()).isEqualTo("pedido.creado");

        Binding dlq = config.stockDlqBinding();
        assertThat(dlq.getExchange()).isEqualTo("pedidos.dlx");
        assertThat(dlq.getDestination()).isEqualTo("pedidos.stock.dlq");
        assertThat(dlq.getRoutingKey()).isEqualTo("pedidos.stock.dead");
    }
}
