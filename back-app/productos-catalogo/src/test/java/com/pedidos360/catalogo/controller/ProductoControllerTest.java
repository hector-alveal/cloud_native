package com.pedidos360.catalogo.controller;

import com.pedidos360.catalogo.model.Producto;
import com.pedidos360.catalogo.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest levanta solo la capa web (controlador + MockMvc), sin base de datos real.
// @AutoConfigureMockMvc(addFilters = false) desactiva los filtros de seguridad (JWT)
// para poder probar la logica del controlador de forma aislada.
@WebMvcTest(ProductoController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoRepository productoRepository;

    @Test
    void pingDevuelveServicioActivo() throws Exception {
        mockMvc.perform(get("/api/productos/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("productos-catalogo activo"));
    }

    @Test
    void listarTodosDevuelveListaDeProductos() throws Exception {
        Producto p1 = new Producto("Hamburguesa Clasica", "Con queso y tocino", 4990.0, 10, "Hamburguesas");
        Producto p2 = new Producto("Papas Fritas", "Porcion individual", 2490.0, 20, "Acompanamientos");

        when(productoRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Hamburguesa Clasica"));
    }

    @Test
    void obtenerPorIdNoEncontradoDevuelve404() throws Exception {
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearProductoDevuelve201() throws Exception {
        Producto guardado = new Producto("Bebida", "Lata 350ml", 1500.0, 50, "Bebidas");
        guardado.setId(1L);

        when(productoRepository.save(any(Producto.class))).thenReturn(guardado);

        String json = """
                {"nombre":"Bebida","descripcion":"Lata 350ml","precio":1500.0,"stock":50,"categoria":"Bebidas"}
                """;

        mockMvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}