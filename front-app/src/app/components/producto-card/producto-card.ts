import { Component, Input } from '@angular/core';
import { DecimalPipe } from '@angular/common';

import { Producto } from '../../components/models/producto';
import { CarritoService } from '../../services/carrito';

@Component({
    selector: 'app-producto-card',
    imports: [DecimalPipe],
    templateUrl: './producto-card.html',
    styleUrl: './producto-card.css'
})
export class ProductCardComponent {

    @Input() producto!: Producto;

    constructor(
        private carritoService: CarritoService
    ) {}

    agregarAlCarrito(): void {

        this.carritoService.agregarProducto(
            this.producto
        );

        alert(
            `${this.producto.nombre} fue agregado al carrito`
        );

    }

}