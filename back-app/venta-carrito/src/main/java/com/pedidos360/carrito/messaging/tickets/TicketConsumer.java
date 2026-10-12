package com.pedidos360.carrito.messaging.tickets;

import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import com.pedidos360.carrito.messaging.comun.ClasificadorErrores;
import com.pedidos360.carrito.messaging.comun.EjecutorConReintentos;
import com.pedidos360.carrito.messaging.comun.LectorMensajes;
import com.pedidos360.carrito.service.TicketService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Dominio TICKETS -> consumidor del evento "pedido.creado" en venta-carrito.
 * Genera el ticket/comprobante del pedido de forma asincrona, para no demorar
 * la respuesta del endpoint de confirmacion.
 *
 * Confirmacion manual (acknowledge-mode: manual):
 * <pre>
 *  Resultado                                        Respuesta al broker
 *  -----------------------------------------------  ------------------------------------------
 *  Ticket generado                                  basicAck                -> sale de la cola
 *  Mensaje invalido o pedido sin productos          basicNack(requeue=false) -> DLQ sin reintentar
 *  Error recuperable que persiste tras reintentos   basicNack(requeue=false) -> DLQ
 *  Servicio detenido en medio de un reintento       basicNack(requeue=true)  -> vuelve a la cola
 * </pre>
 */
@Component
public class TicketConsumer {

    private static final Logger log = LoggerFactory.getLogger(TicketConsumer.class);

    private final TicketService ticketService;
    private final LectorMensajes lector;
    private final EjecutorConReintentos reintentos;

    public TicketConsumer(TicketService ticketService, LectorMensajes lector, EjecutorConReintentos reintentos) {
        this.ticketService = ticketService;
        this.lector = lector;
        this.reintentos = reintentos;
    }

    @RabbitListener(id = "ticket-consumer", queues = "${pedidos360.rabbitmq.colas.ticket.nombre}")
    public void alRecibirPedidoCreado(Message mensaje, Channel canal) throws IOException {
        long deliveryTag = mensaje.getMessageProperties().getDeliveryTag();

        try {
            PedidoCreadoEvent pedido = lector.leer(mensaje, PedidoCreadoEvent.class);
            log.info("[TICKET] Pedido {} recibido, generando comprobante", pedido.pedidoId());

            reintentos.ejecutar("TICKET pedido " + pedido.pedidoId(),
                    () -> log.info("[TICKET] Comprobante generado:{}", ticketService.generarTicket(pedido)));

            canal.basicAck(deliveryTag, false);
            log.info("[TICKET] OK pedido {} -> ACK", pedido.pedidoId());

        } catch (InterruptedException e) {
            log.warn("[TICKET] Consumidor detenido durante un reintento -> NACK con requeue (lo tomara otra instancia)");
            canal.basicNack(deliveryTag, false, true);
            Thread.currentThread().interrupt();

        } catch (Exception e) {
            if (ClasificadorErrores.esNoRecuperable(e)) {
                log.error("[TICKET] Error NO recuperable: {} -> NACK sin reencolar -> DLQ. Mensaje: {}",
                        e.getMessage(), LectorMensajes.cuerpoComoTexto(mensaje));
            } else {
                log.error("[TICKET] Error recuperable que persistio tras {} intentos: {} -> NACK -> DLQ",
                        reintentos.getMaxIntentos(), e.getMessage(), e);
            }
            canal.basicNack(deliveryTag, false, false);
        }
    }
}
