package com.pedidos360.carrito.service;

import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TicketServiceTest {

    private final TicketService ticketService = new TicketService();

    @Test
    void generaUnTicketConLosProductosYElTotal() {
        PedidoCreadoEvent pedido = new PedidoCreadoEvent("PED-1", "user-123",
                List.of(new PedidoCreadoEvent.Item(1L, "Hamburguesa Clasica", 2, 4990.0),
                        new PedidoCreadoEvent.Item(3L, "Bebida", 1, 1500.0)),
                11480.0, LocalDateTime.of(2026, 10, 12, 13, 30));

        String ticket = ticketService.generarTicket(pedido);

        assertThat(ticket)
                .contains("PED-1")
                .contains("Hamburguesa Clasica")
                .contains("Bebida")
                .contains("TOTAL")
                .contains("11480")
                .contains("12-10-2026 13:30");
    }

    @Test
    void unPedidoSinProductosNoGeneraTicket() {
        PedidoCreadoEvent vacio = new PedidoCreadoEvent("PED-2", "user-123", List.of(), 0.0, null);

        assertThatThrownBy(() -> ticketService.generarTicket(vacio))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
