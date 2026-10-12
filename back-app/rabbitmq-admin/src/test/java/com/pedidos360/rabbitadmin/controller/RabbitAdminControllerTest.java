package com.pedidos360.rabbitadmin.controller;

import com.pedidos360.rabbitadmin.dto.CrearColaRequest;
import com.pedidos360.rabbitadmin.exception.RecursoNoEncontradoException;
import com.pedidos360.rabbitadmin.service.RabbitAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de la API REST del administrador: rutas, codigos HTTP y validacion de entrada.
 * Se desactivan los filtros de seguridad (JWT) para probar solo la capa web.
 */
@WebMvcTest(RabbitAdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class RabbitAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RabbitAdminService adminService;

    @Test
    void crearColaValidaDevuelve201YLosRecursosCreados() throws Exception {
        when(adminService.crearCola(any(CrearColaRequest.class)))
                .thenReturn(List.of("pagos.queue", "pagos.queue.dlx", "pagos.queue.dlq"));

        mockMvc.perform(post("/api/rabbitmq/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"pagos.queue","conDlq":true,"ttlMs":60000}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/rabbitmq/queues/pagos.queue"))
                .andExpect(jsonPath("$.recursos.length()").value(3));
    }

    @Test
    void crearColaConNombreVacioDevuelve400YNoLlamaAlServicio() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalles[0]").value(containsString("nombre")));

        verifyNoInteractions(adminService);
    }

    @Test
    void crearColaConPrefijoReservadoAmqDevuelve400() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"amq.mi-cola"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("amq.")));

        verifyNoInteractions(adminService);
    }

    @Test
    void crearColaQuorumNoDurableDevuelve400() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"pagos.queue","tipo":"QUORUM","durable":false}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("QUORUM")));

        verifyNoInteractions(adminService);
    }

    @Test
    void crearColaConTtlNegativoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/queues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"pagos.queue","ttlMs":-5}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("ttlMs")));
    }

    @Test
    void crearExchangeConTipoInexistenteDevuelve400ConLosValoresPermitidos() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"pagos.exchange","tipo":"CIRCULAR"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("DIRECT")));

        verifyNoInteractions(adminService);
    }

    @Test
    void crearBindingSinRoutingKeyDevuelve400() throws Exception {
        mockMvc.perform(post("/api/rabbitmq/bindings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"exchange":"pagos.exchange","cola":"pagos.queue"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("routingKey")));
    }

    @Test
    void eliminarColaExistenteDevuelve204() throws Exception {
        mockMvc.perform(delete("/api/rabbitmq/queues/pagos.queue"))
                .andExpect(status().isNoContent());

        verify(adminService).eliminarCola("pagos.queue");
    }

    @Test
    void eliminarColaInexistenteDevuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe la cola 'fantasma.queue'"))
                .when(adminService).eliminarCola("fantasma.queue");

        mockMvc.perform(delete("/api/rabbitmq/queues/fantasma.queue"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detalles[0]").value(containsString("fantasma.queue")));
    }

    @Test
    void eliminarColaConNombreInvalidoDevuelve400() throws Exception {
        mockMvc.perform(delete("/api/rabbitmq/queues/amq.prueba"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(adminService);
    }
}
