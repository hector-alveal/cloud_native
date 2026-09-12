package com.pedidos360.carrito.controller;

import com.pedidos360.carrito.model.ItemCarrito;
import com.pedidos360.carrito.repository.ItemCarritoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final ItemCarritoRepository itemCarritoRepository;

    @Autowired
    public CarritoController(ItemCarritoRepository itemCarritoRepository) {
        this.itemCarritoRepository = itemCarritoRepository;
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
        return ResponseEntity.ok(itemCarritoRepository.findByUsuarioId(usuarioId));
    }

    @PostMapping
    public ResponseEntity<ItemCarrito> agregarItem(@Valid @RequestBody ItemCarrito item) {
        ItemCarrito guardado = itemCarritoRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCarrito> actualizarCantidad(@PathVariable Long id, @RequestBody ItemCarrito datos) {
        return itemCarritoRepository.findById(id)
                .map(item -> {
                    item.setCantidad(datos.getCantidad());
                    item.setTotal(item.getCantidad() * item.getPrecioUnitario());
                    return ResponseEntity.ok(itemCarritoRepository.save(item));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        if (!itemCarritoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemCarritoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
