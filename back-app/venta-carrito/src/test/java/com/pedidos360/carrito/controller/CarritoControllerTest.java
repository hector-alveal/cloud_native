package com.pedidos360.carrito.controller;

import com.pedidos360.carrito.model.ItemCarrito;
import com.pedidos360.carrito.repository.ItemCarritoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
@AutoConfigureMockMvc(addFilters = false)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemCarritoRepository itemCarritoRepository;

    @Test
    void pingDevuelveServicioActivo() throws Exception {
        mockMvc.perform(get("/api/carrito/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("venta-carrito activo"));
    }

    @Test
    void listarPorUsuarioDevuelveSusItems() throws Exception {
        ItemCarrito item = new ItemCarrito("user-123", 1L, "Hamburguesa Clasica", 2, 4990.0);

        when(itemCarritoRepository.findByUsuarioId("user-123")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/carrito/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombreProducto").value("Hamburguesa Clasica"));
    }

    @Test
    void agregarItemDevuelve201() throws Exception {
        ItemCarrito guardado = new ItemCarrito("user-123", 1L, "Hamburguesa Clasica", 1, 4990.0);
        guardado.setId(1L);

        when(itemCarritoRepository.save(any(ItemCarrito.class))).thenReturn(guardado);

        String json = """
                {"usuarioId":"user-123","productoId":1,"nombreProducto":"Hamburguesa Clasica","cantidad":1,"precioUnitario":4990.0}
                """;

        mockMvc.perform(post("/api/carrito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void eliminarItemInexistenteDevuelve404() throws Exception {
        when(itemCarritoRepository.existsById(99L)).thenReturn(false);

        mockMvc.perform(delete("/api/carrito/99"))
                .andExpect(status().isNotFound());
    }
}