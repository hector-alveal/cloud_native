import { Producto } from './producto';

export interface ItemCarrito {

    producto: Producto;

    cantidad: number;

}

export interface Carrito {

    items: ItemCarrito[];

    total: number;

}