import { TestBed } from '@angular/core/testing';
import { PROVEEDORES_TEST } from './testing/proveedores-test';
import { AppComponent } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: PROVEEDORES_TEST,
    })
      .compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render navbar and footer', async () => {
    const fixture = TestBed.createComponent(AppComponent);
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('app-navbar')).toBeTruthy();
    expect(compiled.querySelector('app-footer')).toBeTruthy();
  });
});
