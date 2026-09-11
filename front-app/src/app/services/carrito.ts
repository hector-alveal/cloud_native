import { Injectable } from '@angular/core';
import { Producto } from '../components/models/producto';
import { ItemCarrito } from '../components/models/carrito';

@Injectable({
    providedIn: 'root'
})
export class CarritoService {

    private items: ItemCarrito[] = [];

    obtenerItems(): ItemCarrito[] {

        return this.items;

    }

    agregarProducto(producto: Producto): void {

        const itemExistente = this.items.find(
            item => item.producto.id === producto.id
        );

        if (itemExistente) {

            itemExistente.cantidad++;

        } else {

            this.items.push({
                producto: producto,
                cantidad: 1
            });

        }

    }

    eliminarProducto(id: number): void {

        this.items = this.items.filter(
            item => item.producto.id !== id
        );

    }

    aumentarCantidad(id: number): void {

        const item = this.items.find(
            item => item.producto.id === id
        );

        if (item) {
            item.cantidad++;
        }

    }

    disminuirCantidad(id: number): void {

        const item = this.items.find(
            item => item.producto.id === id
        );

        if (!item) {
            return;
        }

        if (item.cantidad > 1) {

            item.cantidad--;

        } else {

            this.eliminarProducto(id);

        }

    }

    calcularTotal(): number {

        return this.items.reduce(
            (total, item) =>
                total + (item.producto.precio * item.cantidad),
            0
        );

    }

    vaciarCarrito(): void {

        this.items = [];

    }

}