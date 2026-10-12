package com.pedidos360.rabbitadmin.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Formato unico de error de la API, para que el cliente siempre reciba la misma estructura. */
public record RespuestaError(
        LocalDateTime timestamp,
        int status,
        String error,
        List<String> detalles,
        String ruta) {
}
