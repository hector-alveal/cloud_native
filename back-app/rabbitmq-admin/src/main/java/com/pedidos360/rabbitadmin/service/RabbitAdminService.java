package com.pedidos360.rabbitadmin.service;

import com.pedidos360.rabbitadmin.dto.ColaInfoResponse;
import com.pedidos360.rabbitadmin.dto.CrearBindingRequest;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.dto.CrearExchangeRequest;
import com.pedidos360.rabbitadmin.dto.TipoCola;
import com.pedidos360.rabbitadmin.exception.RecursoDuplicadoException;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Unico punto del microservicio que habla con RabbitMQ.
 * Encapsula la creacion y eliminacion de colas, exchanges y bindings:
 * el controlador solo llama a estos metodos de alto nivel y no conoce RabbitAdmin.
 */
@Service
public class RabbitAdminService {

    private static final Logger log = LoggerFactory.getLogger(RabbitAdminService.class);

    static final String SUFIJO_DLX = ".dlx";
    static final String SUFIJO_DLQ = ".dlq";

    private final RabbitAdmin rabbitAdmin;

    public RabbitAdminService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // ===================================================================== COLAS

    /**
     * Crea una cola. Si se pide DLQ, crea tambien:
     *   - exchange  <nombre>.dlx (direct)
     *   - cola      <nombre>.dlq
     *   - binding   <nombre>.dlx --(<nombre>.dlq)--> <nombre>.dlq
     * y configura la cola principal para que sus mensajes rechazados o expirados vayan ahi.
     *
     * @return nombres de todos los recursos creados
     */
    public List<String> crearCola(CrearColaRequest request) {
        String nombre = request.nombre();
        if (existeCola(nombre)) {
            throw new RecursoDuplicadoException("La cola '" + nombre + "' ya existe");
        }

        QueueBuilder cola = request.esDurable() ? QueueBuilder.durable(nombre) : QueueBuilder.nonDurable(nombre);
        if (request.tipoEfectivo() == TipoCola.QUORUM) {
            cola.withArgument("x-queue-type", "quorum");
        }
        if (request.ttlMs() != null) {
            cola.withArgument("x-message-ttl", request.ttlMs());
        }
        if (request.maxMensajes() != null) {
            cola.withArgument("x-max-length", request.maxMensajes());
        }

        List<String> creados = new ArrayList<>();
        creados.add(nombre);

        if (request.incluyeDlq()) {
            String dlx = nombre + SUFIJO_DLX;
            String dlq = nombre + SUFIJO_DLQ;
            rabbitAdmin.declareExchange(new DirectExchange(dlx, true, false));
            rabbitAdmin.declareQueue(QueueBuilder.durable(dlq).build());
            rabbitAdmin.declareBinding(new Binding(dlq, Binding.DestinationType.QUEUE, dlx, dlq, null));
            cola.withArgument("x-dead-letter-exchange", dlx);
            cola.withArgument("x-dead-letter-routing-key", dlq);
            creados.add(dlx);
            creados.add(dlq);
        }

        rabbitAdmin.declareQueue(cola.build());
        log.info("[ADMIN] Cola creada: {} (tipo={}, durable={}, dlq={})",
                nombre, request.tipoEfectivo(), request.esDurable(), request.incluyeDlq());
        return creados;
    }

    public ColaInfoResponse obtenerCola(String nombre) {
        QueueInformation info = rabbitAdmin.getQueueInfo(nombre);
        if (info == null) {
            throw new RecursoNoEncontradoException("No existe la cola '" + nombre + "'");
        }
        return new ColaInfoResponse(info.getName(), info.getMessageCount(), info.getConsumerCount());
    }

    public void eliminarCola(String nombre) {
        if (!existeCola(nombre)) {
            throw new RecursoNoEncontradoException("No existe la cola '" + nombre + "'");
        }
        rabbitAdmin.deleteQueue(nombre);
        log.info("[ADMIN] Cola eliminada: {}", nombre);
    }

    // ================================================================= EXCHANGES

    public void crearExchange(CrearExchangeRequest request) {
        String nombre = request.nombre();
        if (existeExchange(nombre)) {
            throw new RecursoDuplicadoException("El exchange '" + nombre + "' ya existe");
        }

        boolean durable = request.esDurable();
        Exchange exchange = switch (request.tipo()) {
            case DIRECT -> new DirectExchange(nombre, durable, false);
            case TOPIC -> new TopicExchange(nombre, durable, false);
            case FANOUT -> new FanoutExchange(nombre, durable, false);
            case HEADERS -> new HeadersExchange(nombre, durable, false);
        };

        rabbitAdmin.declareExchange(exchange);
        log.info("[ADMIN] Exchange creado: {} (tipo={}, durable={})", nombre, request.tipo(), durable);
    }

    public void eliminarExchange(String nombre) {
        if (!existeExchange(nombre)) {
            throw new RecursoNoEncontradoException("No existe el exchange '" + nombre + "'");
        }
        rabbitAdmin.deleteExchange(nombre);
        log.info("[ADMIN] Exchange eliminado: {}", nombre);
    }

    // ================================================================== BINDINGS

    public void crearBinding(CrearBindingRequest request) {
        validarQueExisten(request.exchange(), request.cola());
        rabbitAdmin.declareBinding(binding(request.exchange(), request.cola(), request.routingKey()));
        log.info("[ADMIN] Binding creado: {} --({})--> {}", request.exchange(), request.routingKey(), request.cola());
    }

    public void eliminarBinding(String exchange, String cola, String routingKey) {
        validarQueExisten(exchange, cola);
        rabbitAdmin.removeBinding(binding(exchange, cola, routingKey));
        log.info("[ADMIN] Binding eliminado: {} --({})--> {}", exchange, routingKey, cola);
    }

    // ================================================================= AUXILIARES

    private Binding binding(String exchange, String cola, String routingKey) {
        return new Binding(cola, Binding.DestinationType.QUEUE, exchange, routingKey, null);
    }

    private void validarQueExisten(String exchange, String cola) {
        if (!existeExchange(exchange)) {
            throw new RecursoNoEncontradoException("No existe el exchange '" + exchange + "'");
        }
        if (!existeCola(cola)) {
            throw new RecursoNoEncontradoException("No existe la cola '" + cola + "'");
        }
    }

    /** getQueueInfo hace una declaracion pasiva: devuelve null si la cola no existe. */
    private boolean existeCola(String nombre) {
        return rabbitAdmin.getQueueInfo(nombre) != null;
    }

    /** Declaracion pasiva del exchange: si no existe, RabbitMQ responde 404 y se devuelve false. */
    private boolean existeExchange(String nombre) {
        Boolean existe = rabbitAdmin.getRabbitTemplate().execute(canal -> {
            try {
                canal.exchangeDeclarePassive(nombre);
                return Boolean.TRUE;
            } catch (IOException e) {
                return Boolean.FALSE;
            }
        });
        return Boolean.TRUE.equals(existe);
    }
}
