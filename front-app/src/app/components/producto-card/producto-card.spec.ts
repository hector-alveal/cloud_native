import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PROVEEDORES_TEST } from '../../testing/proveedores-test';
import { ProductCardComponent } from './producto-card';

describe('ProductoCard', () => {
  let component: ProductCardComponent;
  let fixture: ComponentFixture<ProductCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductCardComponent],
      providers: PROVEEDORES_TEST,
    }).compileComponents();

    fixture = TestBed.createComponent(ProductCardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('producto', {
      id: 1, nombre: 'Hamburguesa', descripcion: '', precio: 5990, imagen: '', categoria: 'Hamburguesas', stock: 10,
    });
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
