import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Producto } from '../components/models/producto';
import { PRODUCTOS } from '../components/data/producto';
import { environment } from '../../environments/environment';

@Injectable({
    providedIn: 'root'
})


export class ProductoService2 { 
    private productos: Producto[] = PRODUCTOS; 
    obtenerProductos(): Producto[] {
        return this.productos;
        }
        obtenerProductoPorId(id: number): Producto | undefined { 
            return this.productos.find( 
                producto => producto.id === id ); } }

