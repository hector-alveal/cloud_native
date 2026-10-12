package com.pedidos360.rabbitadmin.dto;

/** Tipos de exchange que se pueden crear desde la API. */
public enum TipoExchange {
    DIRECT,
    TOPIC,
    FANOUT,
    HEADERS
}
