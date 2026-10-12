package com.pedidos360.rabbitadmin.dto;

import java.util.List;

/** Respuesta de una creacion exitosa: mensaje + nombres de los recursos creados en RabbitMQ. */
public record RecursoCreadoResponse(String mensaje, List<String> recursos) {
}
