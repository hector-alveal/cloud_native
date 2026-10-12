package com.pedidos360.carrito.controller;

import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import com.pedidos360.carrito.model.ItemCarrito;
import com.pedidos360.carrito.service.CarritoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    @Autowired
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    // Endpoint simple para verificar que el servicio esta arriba (util para probar
    // el API Gateway antes de meter seguridad)
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("venta-carrito activo");
    }

    // Lista los items del carrito de un usuario.
    // Cuando agreguen seguridad, el usuarioId deberia obtenerse del JWT (claim sub/oid)
    // en lugar de venir como parametro de la URL.
    @GetMapping("/{usuarioId}")
    public ResponseEntity<List<ItemCarrito>> listarPorUsuario(@PathVariable String usuarioId) {
        return ResponseEntity.ok(carritoService.listarPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<ItemCarrito> agregarItem(@Valid @RequestBody ItemCarrito item) {
        ItemCarrito guardado = carritoService.agregar(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCarrito> actualizarCantidad(@PathVariable Long id, @RequestBody ItemCarrito datos) {
        return carritoService.actualizarCantidad(id, datos)
            .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        if (!carritoService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    // Confirma el carrito como pedido. Responde 202 Accepted porque el descuento de stock
    // (productos-catalogo) y el ticket (venta-carrito) se procesan despues, via RabbitMQ.
    @PostMapping("/{usuarioId}/confirmar")
    public ResponseEntity<PedidoCreadoEvent> confirmarPedido(@PathVariable("usuarioId") String usuarioId) {
        return ResponseEntity.accepted().body(carritoService.confirmarPedido(usuarioId));
    }
}
