package com.pedidos360.catalogo.exception;

/**
 * Error de negocio: la operacion no tiene sentido con los datos recibidos
 * (producto inexistente, stock insuficiente...). Reintentar no lo arregla.
 */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
