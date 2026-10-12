package com.pedidos360.carrito.messaging.comun;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convierte el cuerpo JSON de un mensaje AMQP al objeto esperado y lo valida.
 * Si no se puede, lanza {@link MensajeInvalidoException} (error no recuperable).
 */
@Component
public class LectorMensajes {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public LectorMensajes(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public <T> T leer(Message mensaje, Class<T> tipo) {
        T objeto;
        try {
            objeto = objectMapper.readValue(mensaje.getBody(), tipo);
        } catch (IOException e) {
            throw new MensajeInvalidoException("El cuerpo no es un JSON valido de " + tipo.getSimpleName(), e);
        }
        if (objeto == null) {
            throw new MensajeInvalidoException("El mensaje llego vacio");
        }

        Set<ConstraintViolation<T>> errores = validator.validate(objeto);
        if (!errores.isEmpty()) {
            String detalle = errores.stream()
                    .map(error -> error.getPropertyPath() + " " + error.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; "));
            throw new MensajeInvalidoException("Datos faltantes o invalidos: " + detalle);
        }
        return objeto;
    }

    /** Cuerpo del mensaje como texto, para dejarlo en los logs. */
    public static String cuerpoComoTexto(Message mensaje) {
        return new String(mensaje.getBody(), StandardCharsets.UTF_8);
    }
}
