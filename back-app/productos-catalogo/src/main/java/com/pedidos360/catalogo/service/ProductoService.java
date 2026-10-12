package com.pedidos360.catalogo.service;

import com.pedidos360.catalogo.exception.ProductoNoEncontradoException;
import com.pedidos360.catalogo.exception.StockInsuficienteException;
import com.pedidos360.catalogo.model.Producto;
import com.pedidos360.catalogo.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Logica de negocio del catalogo. No sabe nada de RabbitMQ: el consumidor
 * de mensajes solo llama a {@link #descontarStock(Map)} como cualquier otro cliente.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return productoRepository.findById(id);
    }

    public List<Producto> listarPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria);
    }

    public Producto crear(Producto producto) {
        return productoRepository.save(producto);
    }

    public Optional<Producto> actualizar(Long id, Producto datos) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(datos.getNombre());
            producto.setDescripcion(datos.getDescripcion());
            producto.setPrecio(datos.getPrecio());
            producto.setStock(datos.getStock());
            producto.setCategoria(datos.getCategoria());
            return productoRepository.save(producto);
        });
    }

    public boolean eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            return false;
        }
        productoRepository.deleteById(id);
        return true;
    }

    /**
     * Descuenta el stock de todos los productos de un pedido en una sola transaccion:
     * si un producto no existe o no alcanza el stock, no se descuenta ninguno.
     *
     * @param cantidadesPorProducto id del producto -> cantidad a descontar
     * @throws ProductoNoEncontradoException si algun producto no existe
     * @throws StockInsuficienteException    si algun producto no tiene stock suficiente
     */
    @Transactional
    public void descontarStock(Map<Long, Integer> cantidadesPorProducto) {
        cantidadesPorProducto.forEach((productoId, cantidad) -> {
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ProductoNoEncontradoException(productoId));

            if (producto.getStock() < cantidad) {
                throw new StockInsuficienteException(productoId, producto.getStock(), cantidad);
            }

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        });
    }
}
