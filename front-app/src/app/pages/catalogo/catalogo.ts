import { Component } from '@angular/core';
import { Producto } from '../../components/models/producto';
import { ProductoService } from '../../services/producto';
import { ProductCardComponent } from '../../components/producto-card/producto-card';

@Component({
    selector: 'app-catalogo',
    imports: [ ProductCardComponent ],
    templateUrl: './catalogo.html',
    styleUrl: './catalogo.css'
})
export class CatalogoComponent {

    productos: Producto[] = [];

    constructor(
        private productoService: ProductoService
    ) {}

    ngOnInit(): void {

        this.productos = this.productoService.obtenerProductos();

    }

}