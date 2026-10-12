package com.pedidos360.carrito.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Nombres y parametros de RabbitMQ leidos desde application.yml (prefijo pedidos360.rabbitmq).
 * Ninguna clase escribe nombres de colas o exchanges "a mano": todas usan este objeto.
 * Si falta alguna propiedad, la aplicacion no arranca (validacion al inicio).
 */
@Validated
@ConfigurationProperties(prefix = "pedidos360.rabbitmq")
public record RabbitMQProperties(
        @Valid @NotNull Exchanges exchanges,
        @Valid @NotNull RoutingKeys routingKeys,
        @Valid @NotNull Colas colas,
        @Valid @NotNull Retencion retencion,
        @Valid @NotNull Reintentos reintentos) {

    public record Exchanges(@NotBlank String pedidos, @NotBlank String deadLetter) {
    }

    public record RoutingKeys(@NotBlank String pedidoCreado) {
    }

    public record Colas(@Valid @NotNull ColaConDlq ticket) {
    }

    /** Una cola de trabajo con su DLQ asociada. */
    public record ColaConDlq(@NotBlank String nombre, @NotBlank String dlq, @NotBlank String dlqRoutingKey) {
    }

    /** Politicas de retencion (TTL y largo maximo). */
    public record Retencion(@Positive int ttlMensajeMs, @Positive int maxMensajes, @Positive int ttlDlqMs) {
    }

    /** Reintentos para errores recuperables, con backoff exponencial. */
    public record Reintentos(@Min(1) int maxIntentos,
                             @PositiveOrZero long intervaloInicialMs,
                             @DecimalMin("1.0") double multiplicador) {
    }
}
