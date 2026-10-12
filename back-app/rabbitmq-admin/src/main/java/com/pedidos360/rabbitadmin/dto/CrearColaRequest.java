package com.pedidos360.rabbitadmin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Datos para crear una cola.
 * Solo "nombre" es obligatorio; el resto tiene valores por defecto seguros.
 */
public record CrearColaRequest(

        @Schema(description = "Nombre de la cola", example = "notificaciones.email.queue")
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE)
        String nombre,

        @Schema(description = "CLASSIC (por defecto) o QUORUM", example = "CLASSIC")
        TipoCola tipo,

        @Schema(description = "Si la cola sobrevive a un reinicio del broker (por defecto true)", example = "true")
        Boolean durable,

        @Schema(description = "Crea además <nombre>.dlx y <nombre>.dlq para los mensajes fallidos", example = "true")
        Boolean conDlq,

        @Schema(description = "Tiempo de vida de cada mensaje en ms (política de retención)", example = "60000")
        @Positive(message = "debe ser mayor a 0")
        @Max(value = Validaciones.TTL_MAXIMO_MS, message = "no puede superar 7 días (604800000 ms)")
        Integer ttlMs,

        @Schema(description = "Cantidad máxima de mensajes que puede acumular la cola", example = "1000")
        @Positive(message = "debe ser mayor a 0")
        @Max(value = 1_000_000, message = "no puede superar 1000000")
        Integer maxMensajes) {

    public TipoCola tipoEfectivo() {
        return tipo == null ? TipoCola.CLASSIC : tipo;
    }

    public boolean esDurable() {
        return durable == null || durable;
    }

    public boolean incluyeDlq() {
        return Boolean.TRUE.equals(conDlq);
    }

    /** Regla cruzada: RabbitMQ no permite colas QUORUM no durables. */
    @JsonIgnore
    @AssertTrue(message = "una cola QUORUM debe ser durable (durable = true)")
    public boolean isQuorumDurable() {
        return tipoEfectivo() != TipoCola.QUORUM || esDurable();
    }
}
