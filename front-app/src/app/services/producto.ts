import { Injectable } from '@angular/core';


import { Producto } from '../components/models/producto';

Injectable({
    providedIn: 'root'
})

import { PRODUCTOS } from '../components/data/producto';

export class ProductoService { 
    private productos: Producto[] = PRODUCTOS; 
    obtenerProductos(): Producto[] {
        return this.productos;
        }
        obtenerProductoPorId(id: number): Producto | undefined { 
            return this.productos.find( 
                producto => producto.id === id ); } }



/*  un pequeñotp cambio de momento
export class ProductoService {

    private http = inject(HttpClient);

    // Llama al microservicio productos-catalogo a traves del API Manager (AWS API Gateway).
    // El JWT se adjunta automaticamente gracias al MsalInterceptor configurado en app.config.ts.
    private baseUrl = `${environment.apiBaseUrl}/api/productos`;

    obtenerProductos(): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.baseUrl);
    }

    obtenerProductoPorId(id: number): Observable<Producto> {
        return this.http.get<Producto>(`${this.baseUrl}/${id}`);
    }

} */
