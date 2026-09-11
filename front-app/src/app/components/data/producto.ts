import { Producto } from '../models/producto';

export const PRODUCTOS: Producto[] = [

    {
        id: 1,
        nombre: 'Hamburguesa Clásica',
        descripcion: 'Carne de vacuno, queso, lechuga, tomate y salsa especial.',
        precio: 5990,
        imagen: 'assets/img/hamburguesa-clasica.jpg',
        categoria: 'Hamburguesas',
        stock: 10
    },

    {
        id: 2,
        nombre: 'Hamburguesa BBQ',
        descripcion: 'Carne de vacuno, queso cheddar, tocino y salsa BBQ.',
        precio: 7490,
        imagen: 'assets/img/hamburguesa-bbq.jpg',
        categoria: 'Hamburguesas',
        stock: 8
    },

    {
        id: 3,
        nombre: 'Hamburguesa Doble',
        descripcion: 'Doble carne, doble queso, cebolla y salsa especial.',
        precio: 8990,
        imagen: 'assets/img/hamburguesa-doble.jpg',
        categoria: 'Hamburguesas',
        stock: 6
    },

    {
        id: 4,
        nombre: 'Papas Fritas',
        descripcion: 'Papas fritas crujientes con sal.',
        precio: 2990,
        imagen: 'assets/img/papas.jpg',
        categoria: 'Acompañamientos',
        stock: 15
    },

    {
        id: 5,
        nombre: 'Bebida',
        descripcion: 'Bebida de 500ml.',
        precio: 1990,
        imagen: 'assets/img/bebida.jpg',
        categoria: 'Bebidas',
        stock: 20
    }

];