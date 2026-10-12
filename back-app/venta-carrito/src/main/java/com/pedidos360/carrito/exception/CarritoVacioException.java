package com.pedidos360.carrito.exception;

/** Se intento confirmar un pedido sin productos en el carrito (HTTP 422). */
public class CarritoVacioException extends RuntimeException {

    public CarritoVacioException(String usuarioId) {
        super("El carrito del usuario " + usuarioId + " esta vacio: no hay nada que confirmar");
    }
}
