package com.pedidos360.carrito.service;

import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import com.pedidos360.carrito.exception.CarritoVacioException;
import com.pedidos360.carrito.model.ItemCarrito;
import com.pedidos360.carrito.repository.ItemCarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CarritoService {

    private final ItemCarritoRepository itemCarritoRepository;
    private final PedidoEventPublisher pedidoEventPublisher;

    public CarritoService(ItemCarritoRepository itemCarritoRepository, PedidoEventPublisher pedidoEventPublisher) {
        this.itemCarritoRepository = itemCarritoRepository;
        this.pedidoEventPublisher = pedidoEventPublisher;
    }

    public List<ItemCarrito> listarPorUsuario(String usuarioId) {
        return itemCarritoRepository.findByUsuarioId(usuarioId);
    }

    public ItemCarrito agregar(ItemCarrito item) {
        return itemCarritoRepository.save(item);
    }

    public Optional<ItemCarrito> actualizarCantidad(Long id, ItemCarrito datos) {
        return itemCarritoRepository.findById(id).map(item -> {
            item.setCantidad(datos.getCantidad());
            item.setTotal(item.getCantidad() * item.getPrecioUnitario());
            return itemCarritoRepository.save(item);
        });
    }

    public boolean eliminar(Long id) {
        if (!itemCarritoRepository.existsById(id)) {
            return false;
        }
        itemCarritoRepository.deleteById(id);
        return true;
    }

    /**
     * Confirma el carrito del usuario como pedido:
     * 1. arma el evento con los productos del carrito,
     * 2. vacia el carrito,
     * 3. publica el evento "pedido.creado" (el stock y el ticket se procesan de forma asincrona).
     * Si la publicacion falla, la transaccion se revierte y el carrito no se pierde.
     *
     * @throws CarritoVacioException si el usuario no tiene productos en el carrito
     */
    @Transactional
    public PedidoCreadoEvent confirmarPedido(String usuarioId) {
        List<ItemCarrito> items = itemCarritoRepository.findByUsuarioId(usuarioId);
        if (items.isEmpty()) {
            throw new CarritoVacioException(usuarioId);
        }

        List<PedidoCreadoEvent.Item> lineas = items.stream()
                .map(item -> new PedidoCreadoEvent.Item(
                        item.getProductoId(), item.getNombreProducto(), item.getCantidad(), item.getPrecioUnitario()))
                .toList();

        double total = items.stream()
                .mapToDouble(item -> item.getCantidad() * item.getPrecioUnitario())
                .sum();

        PedidoCreadoEvent evento = new PedidoCreadoEvent(
                UUID.randomUUID().toString(), usuarioId, lineas, total, LocalDateTime.now());

        itemCarritoRepository.deleteAll(items);
        pedidoEventPublisher.publicarPedidoCreado(evento);
        return evento;
    }
}
