package com.pedidos360.catalogo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Contrato del evento "pedido.creado" que publica venta-carrito al confirmar un pedido.
 * Tiene los mismos campos que com.pedidos360.carrito.dto.PedidoCreadoEvent (se envia como JSON).
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
