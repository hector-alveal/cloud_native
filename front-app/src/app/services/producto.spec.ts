import { TestBed } from '@angular/core/testing';
import { ProductoService2 } from './producto2';

describe('Producto', () => {
  let service: ProductoService2;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProductoService2);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
