package com.pedidos360.carrito.service;

import com.pedidos360.carrito.dto.PedidoCreadoEvent;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * Genera el ticket (comprobante) de un pedido. Es logica de negocio pura:
 * no sabe que se ejecuta a partir de un mensaje de RabbitMQ.
 */
@Service
public class TicketService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String LINEA = "-".repeat(45) + "\n";

    public String generarTicket(PedidoCreadoEvent pedido) {
        if (pedido.items() == null || pedido.items().isEmpty()) {
            throw new IllegalArgumentException("El pedido " + pedido.pedidoId() + " no tiene productos");
        }

        StringBuilder ticket = new StringBuilder();
        ticket.append("\n============ PEDIDOS360 - TICKET ============\n");
        ticket.append("Pedido : ").append(pedido.pedidoId()).append('\n');
        ticket.append("Cliente: ").append(pedido.usuarioId()).append('\n');
        if (pedido.fechaCreacion() != null) {
            ticket.append("Fecha  : ").append(pedido.fechaCreacion().format(FORMATO_FECHA)).append('\n');
        }
        ticket.append(LINEA);

        double total = 0;
        for (PedidoCreadoEvent.Item item : pedido.items()) {
            double precio = item.precioUnitario() == null ? 0 : item.precioUnitario();
            double subtotal = item.cantidad() * precio;
            total += subtotal;
            ticket.append(String.format("%-28s x%-3d $%10.0f%n",
                    recortar(item.nombreProducto()), item.cantidad(), subtotal));
        }

        ticket.append(LINEA);
        ticket.append(String.format("%-33s $%10.0f%n", "TOTAL", total));
        ticket.append("=".repeat(45));
        return ticket.toString();
    }

    private String recortar(String nombre) {
        if (nombre == null) {
            return "(sin nombre)";
        }
        return nombre.length() <= 28 ? nombre : nombre.substring(0, 27) + ".";
    }
}
