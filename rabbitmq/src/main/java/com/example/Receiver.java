package com.example;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class Receiver {
    @RabbitListener(queues = "hello")
    public void receiveMessage(String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
            System.out.println("[" + timestamp + "] [ ✓] Mensaje recibido: '" + message + "'");
        } catch (Exception e) {
            System.err.println("[ ✗] Error procesando mensaje: " +
            e.getMessage());
            e.printStackTrace();
    // Relanzar la excepción para que Spring AMQP maneje el retry
        throw new RuntimeException(e);
        }
    }

        @RabbitListener(queues = "hello")
    public void receiveMessageAdvanced(
        String message,
        org.springframework.amqp.core.Message rawMessage,
        com.rabbitmq.client.Channel channel) // <--- Cambiado aquí
    throws Exception {
        // ... resto de tu código queda igual

    try {
    System.out.println("[ ✓] Mensaje recibido: '" + message + "'");
    System.out.println(" - Content-Type: " +
    rawMessage.getMessageProperties().getContentType());
    System.out.println(" - Timestamp: " +
    rawMessage.getMessageProperties().getTimestamp());


    channel.basicAck(rawMessage.getMessageProperties().getDeliveryTag
    (), false);
    } catch (Exception e) {
    System.err.println("[ ✗] Error: " + e.getMessage());
    channel.basicNack(rawMessage.getMessageProperties().getDeliveryTag(), false, true);
    throw e;
    }
    }
}
