package com.pedidos360.carrito.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento "pedido.creado" que venta-carrito publica al confirmar un pedido.
 * Viaja como JSON; productos-catalogo tiene una copia con los mismos campos.
 * Las anotaciones se usan para rechazar (DLQ) los mensajes incompletos.
 */
public record PedidoCreadoEvent(
        @NotBlank String pedidoId,
        @NotBlank String usuarioId,
        @NotEmpty List<@Valid @NotNull Item> items,
        Double total,
        LocalDateTime fechaCreacion) {

    public record Item(
            @NotNull Long productoId,
            String nombreProducto,
            @NotNull @Positive Integer cantidad,
            Double precioUnitario) {
    }
}
