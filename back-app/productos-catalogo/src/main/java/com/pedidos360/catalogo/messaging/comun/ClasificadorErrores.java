package com.pedidos360.catalogo.messaging.comun;

import com.pedidos360.catalogo.exception.ReglaNegocioException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Decide si un error al procesar un mensaje vale la pena reintentarlo.
 *
 * <pre>
 * Tipo de error                                   Recuperable   Que se hace
 * ----------------------------------------------  -----------   ------------------------------------
 * Mensaje invalido (JSON roto, datos faltantes)    NO           NACK sin reencolar -> DLQ inmediato
 * Regla de negocio (producto no existe, sin stock) NO           NACK sin reencolar -> DLQ inmediato
 * Cualquier otro (BD caida, timeout de red, etc.)  SI           reintentar con backoff; si persiste -> DLQ
 * </pre>
 */
public final class ClasificadorErrores {

    private ClasificadorErrores() {
    }

    public static boolean esNoRecuperable(Throwable error) {
        return error instanceof MensajeInvalidoException
                || error instanceof ReglaNegocioException
                || error instanceof IllegalArgumentException
                || error instanceof DataIntegrityViolationException;
    }
}
