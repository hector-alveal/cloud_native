import { Producto } from './producto';

export interface ItemCarrito {

    producto: Producto;

    cantidad: number;

    // Id de la fila en el backend (venta-carrito). Se usa para actualizar/eliminar el item.
    backendId?: number;

}

export interface Carrito {

    items: ItemCarrito[];

    total: number;

}
