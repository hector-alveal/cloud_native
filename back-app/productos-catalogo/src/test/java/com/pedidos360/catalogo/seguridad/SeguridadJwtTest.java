package com.pedidos360.catalogo.seguridad;

import com.pedidos360.catalogo.config.SecurityConfig;
import com.pedidos360.catalogo.controller.ProductoController;
import com.pedidos360.catalogo.service.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba el filtro JWT real de SecurityConfig: sin token se rechaza (401), con token se atiende.
 * El JwtDecoder se reemplaza por un mock para no depender de Azure AD durante el test.
 */
@WebMvcTest(ProductoController.class)
@Import(SecurityConfig.class)
class SeguridadJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    void sinTokenLaApiDevuelve401() throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void conJwtValidoLaApiResponde200() throws Exception {
        when(productoService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/productos").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void elPingEsPublico() throws Exception {
        mockMvc.perform(get("/api/productos/ping"))
                .andExpect(status().isOk());
    }
}
