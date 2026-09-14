import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CatalogoComponent2 } from './catalogo2';

describe('Catalogo', () => {
  let component: CatalogoComponent2;
  let fixture: ComponentFixture<CatalogoComponent2>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CatalogoComponent2],
    }).compileComponents();

    fixture = TestBed.createComponent(CatalogoComponent2);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
