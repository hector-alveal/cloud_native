package com.pedidos360.catalogo.exception;

public class ProductoNoEncontradoException extends ReglaNegocioException {

    public ProductoNoEncontradoException(Long productoId) {
        super("No existe el producto con id " + productoId);
    }
}
