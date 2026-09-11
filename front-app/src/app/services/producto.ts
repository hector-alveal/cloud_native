import { Injectable } from '@angular/core';
import { Producto } from '../components/models/producto';
import { PRODUCTOS } from '../components/data/producto';

@Injectable({
    providedIn: 'root'
})
export class ProductoService {

    private productos: Producto[] = PRODUCTOS;

    obtenerProductos(): Producto[] {
        return this.productos;
    }

    obtenerProductoPorId(id: number): Producto | undefined {
        return this.productos.find(producto => producto.id === id);
    }

}