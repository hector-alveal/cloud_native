package com.pedidos360.carrito.service;

import com.pedidos360.carrito.dto.PedidoCreadoEvent;

/**
 * Lo unico que la logica de negocio sabe de la mensajeria: "avisa que se creo un pedido".
 * La implementacion con RabbitMQ esta en messaging.pedidos.RabbitPedidoEventPublisher,
 * asi CarritoService no depende de RabbitTemplate, exchanges ni routing keys.
 */
public interface PedidoEventPublisher {

    void publicarPedidoCreado(PedidoCreadoEvent evento);
}
