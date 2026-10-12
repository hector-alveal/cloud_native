package com.pedidos360.rabbitadmin.dto;

/** Tipos de cola soportados por el administrador. */
public enum TipoCola {
    /** Cola clasica de RabbitMQ (tipo por defecto). */
    CLASSIC,
    /** Cola replicada entre los nodos del cluster (requiere durable = true). */
    QUORUM
}
