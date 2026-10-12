package com.pedidos360.rabbitadmin.exception;

/** Se intento crear una cola o exchange que ya existe (HTTP 409). */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
