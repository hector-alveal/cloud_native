package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.config.OpenApiConfig;
import com.pedidos360.rabbitadmin.dto.ColaInfoResponse;
import com.pedidos360.rabbitadmin.dto.CrearBindingRequest;
import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.dto.CrearExchangeRequest;
import com.pedidos360.rabbitadmin.dto.RecursoCreadoResponse;
import com.pedidos360.rabbitadmin.dto.Validaciones;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * API REST del administrador de RabbitMQ.
 *
 * <pre>
 * POST   /api/rabbitmq/queues                 crear cola (opcional: DLQ, TTL, max mensajes, quorum)
 * GET    /api/rabbitmq/queues/{nombre}        ver mensajes y consumidores de una cola
 * DELETE /api/rabbitmq/queues/{nombre}        eliminar cola
 * POST   /api/rabbitmq/exchanges              crear exchange (DIRECT, TOPIC, FANOUT, HEADERS)
 * DELETE /api/rabbitmq/exchanges/{nombre}     eliminar exchange
 * POST   /api/rabbitmq/bindings               enlazar cola y exchange con una routing key
 * DELETE /api/rabbitmq/bindings?exchange=&cola=&routingKey=   eliminar binding
 * </pre>
 *
 * El controlador solo valida la entrada y delega en {@link RabbitAdminService};
 * no conoce ninguna clase de RabbitMQ.
 */
@RestController
@RequestMapping("/api/rabbitmq")
@Validated
@Tag(name = "Administrador RabbitMQ", description = "Crear y eliminar colas, exchanges y bindings")
@SecurityRequirement(name = OpenApiConfig.ESQUEMA_SEGURIDAD)
public class RabbitAdminController {

    private final RabbitAdminService adminService;

    public RabbitAdminController(RabbitAdminService adminService) {
        this.adminService = adminService;
    }

    // ===================================================================== COLAS

    @Operation(summary = "Crear una cola", description = "Con conDlq=true crea además <nombre>.dlx y <nombre>.dlq")
    @PostMapping("/queues")
    public ResponseEntity<RecursoCreadoResponse> crearCola(@Valid @RequestBody CrearColaRequest request) {
        List<String> creados = adminService.crearCola(request);
        return ResponseEntity
                .created(URI.create("/api/rabbitmq/queues/" + request.nombre()))
                .body(new RecursoCreadoResponse("Cola creada correctamente", creados));
    }

    @Operation(summary = "Consultar una cola", description = "Devuelve los mensajes pendientes y consumidores activos")
    @GetMapping("/queues/{nombre}")
    public ResponseEntity<ColaInfoResponse> obtenerCola(
            @PathVariable("nombre")
            @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE) String nombre) {
        return ResponseEntity.ok(adminService.obtenerCola(nombre));
    }

    @Operation(summary = "Eliminar una cola")
    @DeleteMapping("/queues/{nombre}")
    public ResponseEntity<Void> eliminarCola(
            @PathVariable("nombre")
            @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE) String nombre) {
        adminService.eliminarCola(nombre);
        return ResponseEntity.noContent().build();
    }

    // ================================================================= EXCHANGES

    @Operation(summary = "Crear un exchange")
    @PostMapping("/exchanges")
    public ResponseEntity<RecursoCreadoResponse> crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        adminService.crearExchange(request);
        return ResponseEntity
                .created(URI.create("/api/rabbitmq/exchanges/" + request.nombre()))
                .body(new RecursoCreadoResponse("Exchange creado correctamente", List.of(request.nombre())));
    }

    @Operation(summary = "Eliminar un exchange")
    @DeleteMapping("/exchanges/{nombre}")
    public ResponseEntity<Void> eliminarExchange(
            @PathVariable("nombre")
            @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE) String nombre) {
        adminService.eliminarExchange(nombre);
        return ResponseEntity.noContent().build();
    }

    // ================================================================== BINDINGS

    @Operation(summary = "Crear un binding", description = "Enlaza una cola a un exchange con una routing key")
    @PostMapping("/bindings")
    public ResponseEntity<RecursoCreadoResponse> crearBinding(@Valid @RequestBody CrearBindingRequest request) {
        adminService.crearBinding(request);
        String descripcion = request.exchange() + " --(" + request.routingKey() + ")--> " + request.cola();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RecursoCreadoResponse("Binding creado correctamente", List.of(descripcion)));
    }

    @Operation(summary = "Eliminar un binding")
    @DeleteMapping("/bindings")
    public ResponseEntity<Void> eliminarBinding(
            @RequestParam("exchange")
            @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE) String exchange,
            @RequestParam("cola")
            @Pattern(regexp = Validaciones.PATRON_NOMBRE, message = Validaciones.MENSAJE_NOMBRE) String cola,
            @RequestParam(name = "routingKey", defaultValue = "")
            @Pattern(regexp = Validaciones.PATRON_ROUTING_KEY, message = Validaciones.MENSAJE_ROUTING_KEY)
            String routingKey) {
        adminService.eliminarBinding(exchange, cola, routingKey);
        return ResponseEntity.noContent().build();
    }
}
