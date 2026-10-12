package com.pedidos360.carrito.messaging.pedidos;

import com.pedidos360.carrito.config.RabbitMQProperties;
import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitPedidoEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private final RabbitMQProperties props = new RabbitMQProperties(
            new RabbitMQProperties.Exchanges("pedidos.exchange", "pedidos.dlx"),
            new RabbitMQProperties.RoutingKeys("pedido.creado"),
            new RabbitMQProperties.Colas(
                    new RabbitMQProperties.ColaConDlq("pedidos.ticket.queue", "pedidos.ticket.dlq", "pedidos.ticket.dead")),
            new RabbitMQProperties.Retencion(60000, 1000, 86400000),
            new RabbitMQProperties.Reintentos(3, 1000, 2.0));

    @Test
    void publicaEnElExchangeDePedidosConLaRoutingKeyConfiguradaYElIdComoCorrelacion() {
        RabbitPedidoEventPublisher publisher = new RabbitPedidoEventPublisher(rabbitTemplate, props);
        PedidoCreadoEvent evento = new PedidoCreadoEvent("PED-1", "user-123",
                List.of(new PedidoCreadoEvent.Item(1L, "Hamburguesa Clasica", 2, 4990.0)),
                9980.0, LocalDateTime.now());

        publisher.publicarPedidoCreado(evento);

        ArgumentCaptor<CorrelationData> correlacion = ArgumentCaptor.forClass(CorrelationData.class);
        verify(rabbitTemplate).convertAndSend(eq("pedidos.exchange"), eq("pedido.creado"), eq(evento),
                correlacion.capture());
        assertThat(correlacion.getValue().getId()).isEqualTo("PED-1");
    }
}
