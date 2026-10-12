package com.pedidos360.catalogo.messaging.dlq;

import com.pedidos360.catalogo.messaging.comun.LectorMensajes;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Dominio DLQ -> registra en el log cada mensaje que termino en la Dead Letter Queue,
 * con la cola de origen y el motivo que informa RabbitMQ en el header x-death:
 *   rejected = un consumidor hizo NACK sin reencolar
 *   expired  = supero el TTL de la cola sin ser consumido
 *   maxlen   = la cola estaba llena (x-max-length)
 *
 * Se puede apagar con pedidos360.rabbitmq.dlq.auditor-activo=false para que los mensajes
 * queden acumulados en la DLQ y se vean en el dashboard de RabbitMQ.
 */
@Component
public class DlqAuditoriaConsumer {

    private static final Logger log = LoggerFactory.getLogger(DlqAuditoriaConsumer.class);

    @RabbitListener(id = "dlq-stock-auditor",
            queues = "${pedidos360.rabbitmq.colas.stock.dlq}",
            autoStartup = "${pedidos360.rabbitmq.dlq.auditor-activo:true}")
    public void auditar(Message mensaje, Channel canal) throws IOException {
        MessageProperties propiedades = mensaje.getMessageProperties();
        try {
            String colaOrigen = "desconocida";
            String motivo = "desconocido";
            Object veces = "?";

            Object xDeath = propiedades.getHeaders().get("x-death");
            if (xDeath instanceof List<?> muertes && !muertes.isEmpty() && muertes.get(0) instanceof Map<?, ?> ultima) {
                colaOrigen = String.valueOf(ultima.get("queue"));
                motivo = String.valueOf(ultima.get("reason"));
                veces = ultima.get("count");
            }

            log.error("[DLQ] Mensaje en cuarentena | cola origen: {} | motivo: {} | veces: {} | cuerpo: {}",
                    colaOrigen, motivo, veces, LectorMensajes.cuerpoComoTexto(mensaje));
        } finally {
            // El mensaje ya quedo registrado: se confirma para retirarlo de la DLQ.
            canal.basicAck(propiedades.getDeliveryTag(), false);
        }
    }
}
