package com.pedidos360.carrito.messaging.comun;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Decide si un error al procesar un mensaje vale la pena reintentarlo.
 *
 * <pre>
 * Tipo de error                                   Recuperable   Que se hace
 * ----------------------------------------------  -----------   ------------------------------------
 * Mensaje invalido (JSON roto, datos faltantes)    NO           NACK sin reencolar -> DLQ inmediato
 * Datos que no tienen sentido para el negocio      NO           NACK sin reencolar -> DLQ inmediato
 * Cualquier otro (BD caida, timeout de red, etc.)  SI           reintentar con backoff; si persiste -> DLQ
 * </pre>
 */
public final class ClasificadorErrores {

    private ClasificadorErrores() {
    }

    public static boolean esNoRecuperable(Throwable error) {
        return error instanceof MensajeInvalidoException
                || error instanceof IllegalArgumentException
                || error instanceof DataIntegrityViolationException;
    }
}
