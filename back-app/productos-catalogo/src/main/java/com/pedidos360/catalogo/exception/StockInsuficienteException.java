package com.pedidos360.catalogo.exception;

public class StockInsuficienteException extends ReglaNegocioException {

    public StockInsuficienteException(Long productoId, int disponible, int solicitado) {
        super("Stock insuficiente para el producto " + productoId
                + ": disponible " + disponible + ", solicitado " + solicitado);
    }
}
