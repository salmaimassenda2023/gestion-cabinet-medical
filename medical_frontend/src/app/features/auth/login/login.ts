import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SharedButtonComponent } from '../../../shared/components/button/button.component';
import { AuthService } from '../../../core/services/auth.service';
import { UtilisateurService } from '../services/utilisateur.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, SharedButtonComponent],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class LoginComponent {
  username = signal('');
  password = signal('');
  isLoading = signal(false);
  errorMessage = signal('');

  constructor(
    private router: Router,
    private authService: AuthService,
    private utilisateurService: UtilisateurService
  ) { }

  onSubmit() {
    if (!this.username() || !this.password()) {
      this.errorMessage.set('Veuillez remplir tous les champs');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    this.authService.login(this.username(), this.password()).subscribe({
      next: () => {
        this.fetchProfileAndRedirect();
      },
      error: (err) => {
        this.isLoading.set(false);
        const detail = err.error?.error_description || err.message || 'Erreur inconnue';
        this.errorMessage.set(`Erreur: ${detail} (Status: ${err.status})`);
        console.error('Login error:', err);
      }
    });
  }

  private fetchProfileAndRedirect() {
    this.utilisateurService.getCurrentUser().subscribe({
      next: (user) => {
        this.isLoading.set(false);
        this.redirectBasedOnRole(user.role);
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Erreur lors de la récupération du profil');
        console.error('Profile fetch error:', err);
      }
    });
  }

  private redirectBasedOnRole(role: string) {
    switch (role) {
      case 'SUPER_ADMIN':
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'MEDECIN':
        this.router.navigate(['/doctor']);
        break;
      case 'SECRETAIRE':
        this.router.navigate(['/secretary/calendar']);
        break;
      default:
        this.router.navigate(['/']);
        break;
    }
  }
}