import { Component, OnInit } from '@angular/core';
import { Producto } from '../../components/models/producto';
import { ProductoService } from '../../services/producto';
import { ProductCardComponent } from '../../components/producto-card/producto-card';

@Component({
    selector: 'app-catalogo',
    imports: [ ProductCardComponent ],
    templateUrl: './catalogo.html',
    styleUrl: './catalogo.css'
})
export class CatalogoComponent implements OnInit {

    productos: Producto[] = [];
    cargando = true;
    error = '';

    constructor(
        private productoService: ProductoService
    ) {}

    ngOnInit(): void {

        this.productoService.obtenerProductos().subscribe({
            next: (productos) => {
                this.productos = productos;
                this.cargando = false;
            },
            error: (err) => {
                console.error('Error al obtener productos desde el API Gateway', err);
                this.error = 'No se pudieron cargar los productos. Verifica tu sesion y la conexion con el backend.';
                this.cargando = false;
            }
        });

    }

}
