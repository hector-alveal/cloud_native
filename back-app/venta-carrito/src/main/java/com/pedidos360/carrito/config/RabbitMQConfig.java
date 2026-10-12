package com.pedidos360.carrito.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * Topologia RabbitMQ del microservicio venta-carrito.
 * Todos los nombres vienen de application.yml a traves de {@link RabbitMQProperties}.
 *
 * <h2>Productor: evento "pedido.creado"</h2>
 * CarritoService -> PedidoEventPublisher -> [pedidos.exchange] con routing key "pedido.creado".
 * El exchange reparte una copia a cada cola enlazada con esa routing key:
 * <pre>
 *                                    +--> (pedidos.stock.queue)  -> productos-catalogo: descuenta stock
 *  [pedidos.exchange] "pedido.creado"|
 *                                    +--> (pedidos.ticket.queue) -> venta-carrito: genera el ticket
 * </pre>
 *
 * <h2>Caso de uso de este servicio: generar el ticket del pedido</h2>
 * <pre>
 *  (pedidos.ticket.queue) --> TicketConsumer
 *         |  NACK / TTL vencido / cola llena
 *         v
 *  [pedidos.dlx] --"pedidos.ticket.dead"--> (pedidos.ticket.dlq) --> DlqAuditoriaConsumer (log)
 * </pre>
 */
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    private final RabbitMQProperties props;

    public RabbitMQConfig(RabbitMQProperties props) {
        this.props = props;
    }

    // ================================================================ EXCHANGES

    /** Exchange principal donde se publican los eventos de pedidos. */
    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(props.exchanges().pedidos(), true, false);
    }

    /** Dead Letter Exchange: recibe lo que las colas de trabajo no pudieron procesar. */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(props.exchanges().deadLetter(), true, false);
    }

    // ========================================= CASO DE USO: TICKET DEL PEDIDO CREADO

    @Bean
    public Queue ticketQueue() {
        RabbitMQProperties.ColaConDlq cola = props.colas().ticket();
        return QueueBuilder.durable(cola.nombre())
                .withArgument("x-dead-letter-exchange", props.exchanges().deadLetter())
                .withArgument("x-dead-letter-routing-key", cola.dlqRoutingKey())
                .withArgument("x-message-ttl", props.retencion().ttlMensajeMs())
                .withArgument("x-max-length", props.retencion().maxMensajes())
                .build();
    }

    @Bean
    public Binding ticketBinding() {
        return BindingBuilder.bind(ticketQueue())
                .to(pedidosExchange())
                .with(props.routingKeys().pedidoCreado());
    }

    /** DLQ de tickets: guarda los pedidos cuyo ticket no se pudo generar, 24 h para analisis. */
    @Bean
    public Queue ticketDlq() {
        return QueueBuilder.durable(props.colas().ticket().dlq())
                .withArgument("x-message-ttl", props.retencion().ttlDlqMs())
                .build();
    }

    @Bean
    public Binding ticketDlqBinding() {
        return BindingBuilder.bind(ticketDlq())
                .to(deadLetterExchange())
                .with(props.colas().ticket().dlqRoutingKey());
    }

    // ================================================================ PRODUCTOR

    /**
     * RabbitTemplate usado para publicar:
     * - convierte los eventos a JSON;
     * - ConfirmCallback: el broker confirma (ack) o rechaza (nack) cada publicacion;
     * - mandatory + ReturnsCallback: si un mensaje no llega a ninguna cola
     *   (routing key sin binding) se registra en el log en vez de perderse en silencio.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        template.setMandatory(true);

        template.setConfirmCallback((correlacion, ack, causa) -> {
            String id = correlacion != null ? correlacion.getId() : "sin-id";
            if (ack) {
                log.info("[PRODUCTOR] Broker confirmo la recepcion del mensaje {}", id);
            } else {
                log.error("[PRODUCTOR] Broker RECHAZO el mensaje {}: {}", id, causa);
            }
        });

        template.setReturnsCallback(devuelto -> log.error(
                "[PRODUCTOR] Mensaje NO ENTREGADO a ninguna cola | exchange: {} | routing key: {} | motivo: {} {} | cuerpo: {}",
                devuelto.getExchange(), devuelto.getRoutingKey(), devuelto.getReplyCode(), devuelto.getReplyText(),
                new String(devuelto.getMessage().getBody(), StandardCharsets.UTF_8)));

        return template;
    }
}
