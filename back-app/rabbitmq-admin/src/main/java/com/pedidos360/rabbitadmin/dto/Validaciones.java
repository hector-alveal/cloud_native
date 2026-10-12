package com.pedidos360.rabbitadmin.dto;

/**
 * Reglas de validacion compartidas por los DTO y los parametros de ruta del administrador.
 * Estan centralizadas aqui para que POST y DELETE validen exactamente lo mismo.
 */
public final class Validaciones {

    private Validaciones() {
    }

    /**
     * Nombre de cola o exchange: 3 a 100 caracteres, empieza con letra o numero,
     * solo letras, numeros, '.', '_' o '-'. No puede empezar con "amq."
     * porque RabbitMQ reserva ese prefijo para sus recursos internos.
     */
    public static final String PATRON_NOMBRE = "^(?!amq\\.)[a-zA-Z0-9][a-zA-Z0-9._-]{2,99}$";

    public static final String MENSAJE_NOMBRE =
            "debe tener entre 3 y 100 caracteres (letras, números, '.', '_' o '-'), "
            + "empezar con letra o número y no puede empezar con 'amq.' (reservado por RabbitMQ)";

    /** Routing key: puede ser vacia (exchanges FANOUT) y admite los comodines * y # de los TOPIC. */
    public static final String PATRON_ROUTING_KEY = "^[a-zA-Z0-9._*#-]{0,255}$";

    public static final String MENSAJE_ROUTING_KEY =
            "solo admite letras, números y los caracteres . _ - * # (máximo 255)";

    /** TTL maximo permitido para los mensajes de una cola: 7 dias. */
    public static final long TTL_MAXIMO_MS = 604_800_000L;
}
