import { Component, OnInit } from '@angular/core';

import { Producto } from '../../components/models/producto';
import { ProductoService2 } from '../../services/producto2';
import { ProductCardComponent } from '../../components/producto-card/producto-card';

@Component({
    selector: 'app-catalogo',
    imports: [ ProductCardComponent ],
    templateUrl: './catalogo.html',
    styleUrl: './catalogo.css'
})
export class CatalogoComponent2{

    productos: Producto[] = [];
    

    constructor(
        private productoService: ProductoService2
    ) {}

    ngOnInit(): void {

        this.productos = this.productoService.obtenerProductos();


    }

}
