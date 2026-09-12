package com.pedidos360.carrito.repository;

import com.pedidos360.carrito.model.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {

    List<ItemCarrito> findByUsuarioId(String usuarioId);

    void deleteByUsuarioIdAndId(String usuarioId, Long id);
}
