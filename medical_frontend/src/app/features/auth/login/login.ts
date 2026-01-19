import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
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
    private route: ActivatedRoute,
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

    console.log('🔐 Starting login for:', this.username());

    this.authService.login(this.username(), this.password()).subscribe({
      next: (response) => {
        console.log('✅ Login successful');
        console.log('📦 Response:', response);
        
        // Verify token is stored
        const token = this.authService.getToken();
        const storedToken = localStorage.getItem('auth_token');
        
        console.log('🔑 Token from AuthService:', token ? 'EXISTS ✅' : 'MISSING ❌');
        console.log('💾 Token in localStorage:', storedToken ? 'EXISTS ✅' : 'MISSING ❌');
        console.log('📏 Token length:', token?.length);
        
        if (!token) {
          console.error('❌ CRITICAL: Token not stored after login!');
          this.errorMessage.set('Erreur: Token non stocké');
          this.isLoading.set(false);
          return;
        }
        
        // Decode and log token info using AuthService methods
        try {
          const decoded = this.authService.getDecodedToken();
          console.log('👤 Token user:', decoded?.preferred_username);
          console.log('🎭 Token roles:', decoded?.realm_access?.roles);
          console.log('⏰ Token expires:', decoded?.exp ? new Date(decoded.exp * 1000) : 'Unknown');
        } catch (e) {
          console.error('❌ Failed to decode token:', e);
        }
        
        this.fetchProfileAndRedirect();
      },
      error: (err) => {
        this.isLoading.set(false);
        const detail = err.error?.error_description || err.message || 'Erreur inconnue';
        this.errorMessage.set(`Erreur: ${detail} (Status: ${err.status})`);
        console.error('❌ Login error:', err);
      }
    });
  }

  private fetchProfileAndRedirect() {
    console.log('📋 Fetching user profile...');
    
    const token = localStorage.getItem('auth_token');
    console.log('🔑 Token before profile fetch:', token ? 'EXISTS ✅' : 'MISSING ❌');
    
    this.utilisateurService.getCurrentUser().subscribe({
      next: (user) => {
        console.log('✅ Profile fetched:', user);
        console.log('🎭 User role:', user.role);
        this.isLoading.set(false);
        
        const returnUrl = this.route.snapshot.queryParams['returnUrl'];
        if (returnUrl) {
          console.log('🔙 Redirecting to return URL:', returnUrl);
          this.router.navigateByUrl(returnUrl);
        } else {
          this.redirectBasedOnRole(user.role);
        }
      },
      error: (err) => {
        this.isLoading.set(false);
        this.errorMessage.set('Erreur lors de la récupération du profil');
        console.error('❌ Profile fetch error:', err);
        console.error('❌ Error status:', err.status);
        console.error('❌ Error details:', err.error);
      }
    });
  }

  private redirectBasedOnRole(role: string) {
    console.log('🔀 Redirecting user with role:', role);
    
    switch (role) {
      case 'SUPER_ADMIN':
      case 'ADMIN':
        this.router.navigate(['/admin']);
        break;
      case 'MEDECIN':
        this.router.navigate(['/doctor']);
        break;
      case 'SECRETAIRE':
        this.router.navigate(['/secretary']);
        break;
      default:
        this.router.navigate(['/']);
        break;
    }
  }

  
}