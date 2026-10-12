import { TestBed } from '@angular/core/testing';
import { PROVEEDORES_TEST } from '../testing/proveedores-test';
import { CarritoService } from './carrito';

describe('Carrito', () => {
  let service: CarritoService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: PROVEEDORES_TEST });
    service = TestBed.inject(CarritoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
