import { Component, OnInit } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { CarritoService } from '../../services/carrito';
import { ItemCarrito } from '../../components/models/carrito';

@Component({
    selector: 'app-carrito',
    imports: [DecimalPipe],
    templateUrl: './carrito.html',
    styleUrl: './carrito.css'
})
export class CarritoComponent implements OnInit {

    items: ItemCarrito[] = [];
    cargando = true;

    constructor(
        private carritoService: CarritoService
    ) {}

    ngOnInit(): void {

        this.cargarCarrito();

    }

    aumentar(item: ItemCarrito): void {

        if (!item.backendId) return;

        this.carritoService
            .actualizarCantidad(item.backendId, item.cantidad + 1)
            .subscribe(() => this.cargarCarrito());

    }

    disminuir(item: ItemCarrito): void {

        if (!item.backendId) return;

        if (item.cantidad <= 1) {
            this.eliminar(item);
            return;
        }

        this.carritoService
            .actualizarCantidad(item.backendId, item.cantidad - 1)
            .subscribe(() => this.cargarCarrito());

    }

    eliminar(item: ItemCarrito): void {

        if (!item.backendId) return;

        this.carritoService
            .eliminarItem(item.backendId)
            .subscribe(() => this.cargarCarrito());

    }

    vaciar(): void {

        this.items
            .filter((item) => item.backendId)
            .forEach((item) => {
                this.carritoService.eliminarItem(item.backendId!).subscribe();
            });

        // Pequeña espera para que las eliminaciones lleguen al backend antes de refrescar
        setTimeout(() => this.cargarCarrito(), 400);

    }

    obtenerTotal(): number {

        return this.items.reduce(
            (total, item) => total + (item.producto.precio * item.cantidad),
            0
        );

    }

    private cargarCarrito(): void {

        this.cargando = true;

        this.carritoService.obtenerItems().subscribe({
            next: (items) => {
                this.items = items;
                this.cargando = false;
            },
            error: (err) => {
                console.error('Error al obtener el carrito desde el API Gateway', err);
                this.cargando = false;
            }
        });

    }

}
