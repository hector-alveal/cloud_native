import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MsalService } from '@azure/msal-angular';

import { NavbarComponent } from './components/navbar/navbar';
import { FooterComponent } from './components/footer/footer';

@Component({
  selector: 'app-root',
  imports: [
    RouterOutlet,
    NavbarComponent,
    FooterComponent
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {

  private msalService = inject(MsalService);

  ngOnInit(): void {
    // Procesa la respuesta de Azure AD cuando el usuario vuelve del login (loginRedirect).
    this.msalService.handleRedirectObservable().subscribe();
  }

}
