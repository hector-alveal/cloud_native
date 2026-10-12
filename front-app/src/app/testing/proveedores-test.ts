import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { MsalBroadcastService, MsalService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { of } from 'rxjs';

// Reemplazo de MsalService para los tests: simula que no hay sesion iniciada,
// sin conectarse a Azure AD.
const msalServiceFalso = {
  instance: {
    getActiveAccount: () => null,
    getAllAccounts: () => [],
    setActiveAccount: () => undefined,
  },
  handleRedirectObservable: () => of(null),
  loginRedirect: () => of(undefined),
  logoutRedirect: () => of(undefined),
};

// Proveedores comunes para los tests de componentes y servicios:
// router vacio, HttpClient que no sale a la red y MSAL simulado.
export const PROVEEDORES_TEST = [
  provideRouter([]),
  provideHttpClient(),
  provideHttpClientTesting(),
  { provide: MsalService, useValue: msalServiceFalso },
  { provide: MsalBroadcastService, useValue: { inProgress$: of(InteractionStatus.None) } },
];
