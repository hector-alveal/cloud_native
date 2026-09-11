
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  username = '';
  password = '';
  errorMessage = '';

  constructor(
    private router: Router
  ) {}

  onSubmit(): void {

    if (this.username === 'admin' && this.password === '1234') {

      alert('¡Inicio de sesión exitoso!');

      this.errorMessage = '';

      // Después del login volvemos al inicio
      this.router.navigate(['/']);

    } else {

      this.errorMessage = 'Usuario o contraseña incorrectos';

    }
  }
}

