package com.pedidos360.rabbitadmin.dto;

/** Estado actual de una cola en el broker. */
public record ColaInfoResponse(String nombre, int mensajes, int consumidores) {
}
