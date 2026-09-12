import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MsalBroadcastService, MsalService } from '@azure/msal-angular';
import { InteractionStatus } from '@azure/msal-browser';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-navbar',
  imports: [
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css'
})
export class NavbarComponent implements OnInit {

  private msalService = inject(MsalService);
  private msalBroadcastService = inject(MsalBroadcastService);

  nombreUsuario: string | null = null;

  ngOnInit(): void {
    this.msalBroadcastService.inProgress$
      .pipe(filter((status) => status === InteractionStatus.None))
      .subscribe(() => {
        const cuenta = this.msalService.instance.getActiveAccount();
        this.nombreUsuario = cuenta?.name ?? cuenta?.username ?? null;
      });
  }

  cerrarSesion(): void {
    this.msalService.logoutRedirect();
  }
}
