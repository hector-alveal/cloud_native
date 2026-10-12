package com.pedidos360.rabbitadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Datos para enlazar (binding) una cola a un exchange con una routing key. */
public record CrearBindingRequest(

        @Schema(description = "Exchange de origen (debe existir)", example = "notificaciones.exchange")
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE)
        String exchange,

        @Schema(description = "Cola de destino (debe existir)", example = "notificaciones.email.queue")
        @NotBlank(message = "es obligatorio")
        @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE)
        String cola,

        @Schema(description = "Routing key. Use \"\" para exchanges FANOUT", example = "notificacion.email")
        @NotNull(message = "es obligatorio (use \"\" para exchanges FANOUT)")
        @Pattern(regexp = Validaciones.PATRON_ROUTING_KEY, message = Validaciones.MENSAJE_ROUTING_KEY)
        String routingKey) {
}
