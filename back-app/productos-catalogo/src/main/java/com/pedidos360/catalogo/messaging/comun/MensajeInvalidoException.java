package com.pedidos360.catalogo.messaging.comun;

/**
 * El mensaje recibido no se puede interpretar (JSON mal formado o datos obligatorios faltantes).
 * Es un error NO recuperable: reintentar siempre fallaria igual, por eso va directo a la DLQ.
 */
public class MensajeInvalidoException extends RuntimeException {

    public MensajeInvalidoException(String mensaje) {
        super(mensaje);
    }

    public MensajeInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
