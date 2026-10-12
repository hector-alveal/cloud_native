package com.pedidos360.carrito.messaging.pedidos;

import com.pedidos360.carrito.config.RabbitMQProperties;
import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import com.pedidos360.carrito.service.PedidoEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Dominio PEDIDOS -> productor. Implementacion con RabbitMQ de {@link PedidoEventPublisher}.
 * El exchange y la routing key se leen desde application.yml.
 */
@Component
public class RabbitPedidoEventPublisher implements PedidoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitPedidoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public RabbitPedidoEventPublisher(RabbitTemplate rabbitTemplate, RabbitMQProperties props) {
        this.rabbitTemplate = rabbitTemplate;
        this.props = props;
    }

    @Override
    public void publicarPedidoCreado(PedidoCreadoEvent evento) {
        String exchange = props.exchanges().pedidos();
        String routingKey = props.routingKeys().pedidoCreado();

        // El id del pedido viaja como correlacion para identificarlo en el ConfirmCallback
        rabbitTemplate.convertAndSend(exchange, routingKey, evento, new CorrelationData(evento.pedidoId()));

        log.info("[PRODUCTOR] Pedido {} publicado en '{}' con routing key '{}' ({} producto(s), total {})",
                evento.pedidoId(), exchange, routingKey, evento.items().size(), evento.total());
    }
}
