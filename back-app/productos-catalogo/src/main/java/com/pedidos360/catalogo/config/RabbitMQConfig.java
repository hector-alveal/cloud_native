package com.pedidos360.catalogo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia RabbitMQ del microservicio productos-catalogo.
 * Todos los nombres vienen de application.yml a traves de {@link RabbitMQProperties}.
 *
 * Caso de uso: descontar stock cuando se confirma un pedido
 * <pre>
 *  venta-carrito: POST /api/carrito/{usuarioId}/confirmar
 *        |  publica con routing key "pedido.creado"
 *        v
 *  [pedidos.exchange] (direct) --"pedido.creado"--> (pedidos.stock.queue) --> StockPedidoConsumer
 *                                                          |  NACK / TTL vencido / cola llena
 *                                                          v
 *  [pedidos.dlx] (direct) --"pedidos.stock.dead"--> (pedidos.stock.dlq) --> DlqAuditoriaConsumer (log)
 * </pre>
 *
 * Los exchanges tambien los declara venta-carrito con los mismos parametros: declarar un recurso
 * que ya existe es idempotente, y asi no importa que microservicio arranque primero.
 */
@Configuration
@EnableConfigurationProperties(RabbitMQProperties.class)
public class RabbitMQConfig {

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

    // ======================================= CASO DE USO: STOCK DE UN PEDIDO CREADO

    /**
     * Cola de trabajo del stock. Sus argumentos definen que pasa con un mensaje que falla:
     * x-dead-letter-*  -> a donde va si se rechaza (NACK), expira o la cola se llena
     * x-message-ttl    -> retencion maxima esperando a ser consumido
     * x-max-length     -> tope de mensajes acumulados
     */
    @Bean
    public Queue stockQueue() {
        RabbitMQProperties.ColaConDlq cola = props.colas().stock();
        return QueueBuilder.durable(cola.nombre())
                .withArgument("x-dead-letter-exchange", props.exchanges().deadLetter())
                .withArgument("x-dead-letter-routing-key", cola.dlqRoutingKey())
                .withArgument("x-message-ttl", props.retencion().ttlMensajeMs())
                .withArgument("x-max-length", props.retencion().maxMensajes())
                .build();
    }

    @Bean
    public Binding stockBinding() {
        return BindingBuilder.bind(stockQueue())
                .to(pedidosExchange())
                .with(props.routingKeys().pedidoCreado());
    }

    /** DLQ del stock: guarda los pedidos que no se pudieron descontar, 24 h para analisis. */
    @Bean
    public Queue stockDlq() {
        return QueueBuilder.durable(props.colas().stock().dlq())
                .withArgument("x-message-ttl", props.retencion().ttlDlqMs())
                .build();
    }

    @Bean
    public Binding stockDlqBinding() {
        return BindingBuilder.bind(stockDlq())
                .to(deadLetterExchange())
                .with(props.colas().stock().dlqRoutingKey());
    }
}
