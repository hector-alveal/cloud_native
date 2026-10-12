import { TestBed } from '@angular/core/testing';
import { PROVEEDORES_TEST } from '../testing/proveedores-test';
import { ProductoService2 } from './producto2';

describe('Producto', () => {
  let service: ProductoService2;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: PROVEEDORES_TEST });
    service = TestBed.inject(ProductoService2);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
