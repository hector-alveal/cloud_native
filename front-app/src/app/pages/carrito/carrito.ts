import { Component } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { CarritoService } from '../../services/carrito';
import { ItemCarrito } from '../../components/models/carrito';

@Component({
    selector: 'app-carrito',
    imports: [DecimalPipe],
    templateUrl: './carrito.html',
    styleUrl: './carrito.css'
})
export class CarritoComponent {

    items: ItemCarrito[] = [];

    constructor(
        private carritoService: CarritoService
    ) {}

    ngOnInit(): void {

        this.items =
            this.carritoService.obtenerItems();

    }

    aumentar(id: number): void {

        this.carritoService.aumentarCantidad(id);

        this.actualizarCarrito();

    }

    disminuir(id: number): void {

        this.carritoService.disminuirCantidad(id);

        this.actualizarCarrito();

    }

    eliminar(id: number): void {

        this.carritoService.eliminarProducto(id);

        this.actualizarCarrito();

    }

    vaciar(): void {

        this.carritoService.vaciarCarrito();

        this.actualizarCarrito();

    }

    obtenerTotal(): number {

        return this.carritoService.calcularTotal();

    }

    private actualizarCarrito(): void {

        this.items =
            this.carritoService.obtenerItems();

    }

}