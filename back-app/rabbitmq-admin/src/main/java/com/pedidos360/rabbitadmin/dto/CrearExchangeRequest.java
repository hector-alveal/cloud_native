package com.pedidos360.rabbitadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Datos para crear un exchange. */
public record CrearExchangeRequest(

        @Schema(description = "Nombre del exchange", example = "notificaciones.exchange")
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE)
        String nombre,

        @Schema(description = "DIRECT, TOPIC, FANOUT o HEADERS", example = "DIRECT")
        @NotNull(message = "es obligatorio (DIRECT, TOPIC, FANOUT o HEADERS)")
        TipoExchange tipo,

        @Schema(description = "Si el exchange sobrevive a un reinicio del broker (por defecto true)", example = "true")
        Boolean durable) {

    public boolean esDurable() {
        return durable == null || durable;
    }
}
