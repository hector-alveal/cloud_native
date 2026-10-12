package com.pedidos360.carrito.messaging.tickets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import com.pedidos360.carrito.messaging.comun.EjecutorConReintentos;
import com.pedidos360.carrito.messaging.comun.LectorMensajes;
import com.pedidos360.carrito.service.TicketService;
import com.rabbitmq.client.Channel;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketConsumerTest {

    private static final long DELIVERY_TAG = 11L;

    @Mock
    private TicketService ticketService;

    @Mock
    private Channel canal;

    private TicketConsumer consumer;

    @BeforeEach
    void setUp() {
        LectorMensajes lector = new LectorMensajes(
                new ObjectMapper().findAndRegisterModules(),
                Validation.buildDefaultValidatorFactory().getValidator());
        consumer = new TicketConsumer(ticketService, lector, new EjecutorConReintentos(3, 1, 1.0));
    }

    private Message mensaje(String cuerpo) {
        MessageProperties propiedades = new MessageProperties();
        propiedades.setDeliveryTag(DELIVERY_TAG);
        return new Message(cuerpo.getBytes(StandardCharsets.UTF_8), propiedades);
    }

    @Test
    void pedidoValidoGeneraElTicketYHaceAck() throws Exception {
        when(ticketService.generarTicket(any(PedidoCreadoEvent.class))).thenReturn("TICKET");

        consumer.alRecibirPedidoCreado(mensaje("""
                {"pedidoId":"PED-1","usuarioId":"user-123","total":9980.0,
                 "items":[{"productoId":1,"nombreProducto":"Hamburguesa","cantidad":2,"precioUnitario":4990.0}]}
                """), canal);

        verify(ticketService).generarTicket(any(PedidoCreadoEvent.class));
        verify(canal).basicAck(DELIVERY_TAG, false);
        verify(canal, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void mensajeSinPedidoIdVaALaDlqSinGenerarTicket() throws Exception {
        consumer.alRecibirPedidoCreado(mensaje("""
                {"usuarioId":"user-123","items":[{"productoId":1,"cantidad":2}]}
                """), canal);

        verifyNoInteractions(ticketService);
        verify(canal).basicNack(DELIVERY_TAG, false, false);
        verify(canal, never()).basicAck(anyLong(), anyBoolean());
    }
}
