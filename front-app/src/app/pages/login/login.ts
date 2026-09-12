import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MsalBroadcastService, MsalService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  imports: [],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent implements OnInit {

  private msalService = inject(MsalService);
  private msalBroadcastService = inject(MsalBroadcastService);
  private router = inject(Router);

  ngOnInit(): void {
    // Espera a que MSAL termine de procesar cualquier redireccion pendiente
    // antes de revisar si ya hay una cuenta activa.
    this.msalBroadcastService.inProgress$
      .pipe(filter((status) => status === InteractionStatus.None))
      .subscribe(() => {
        if (this.msalService.instance.getActiveAccount()) {
          this.router.navigate(['/']);
        }
      });
  }

  // Dispara el flujo OAuth 2.0 / OpenID Connect con Authorization Code + PKCE
  // (MSAL lo maneja internamente al usar loginRedirect).
  iniciarSesionConMicrosoft(): void {
    this.msalService.loginRedirect();
  }
}
