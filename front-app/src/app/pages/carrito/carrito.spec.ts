import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PROVEEDORES_TEST } from '../../testing/proveedores-test';
import { CarritoComponent } from './carrito';

describe('Carrito', () => {
  let component: CarritoComponent;
  let fixture: ComponentFixture<CarritoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CarritoComponent],
      providers: PROVEEDORES_TEST,
    }).compileComponents();

    fixture = TestBed.createComponent(CarritoComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
