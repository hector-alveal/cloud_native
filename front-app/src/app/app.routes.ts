import { Routes } from '@angular/router';

import { HomeComponent } from './pages/home/home';
import { CatalogoComponent } from './pages/catalogo/catalogo';
import { CarritoComponent } from './pages/carrito/carrito';
import { LoginComponent } from './pages/login/login';

export const routes: Routes = [

    {
        path: '',
        component: HomeComponent
    },

    {
        path: 'productos',
        component: CatalogoComponent
    },

    {
        path: 'carrito',
        component: CarritoComponent
    },

    { 
        path: 'login', 
        component: LoginComponent 
    }

];