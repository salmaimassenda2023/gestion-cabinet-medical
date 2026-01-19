import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-unauthorized',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="unauthorized-container">
      <div class="unauthorized-content">
        <div class="icon">🚫</div>
        <h1>Access Denied</h1>
        <p>You don't have permission to access this page.</p>
        <p class="details">Your current role(s): <strong>{{ userRoles }}</strong></p>
        <div class="actions">
          <button (click)="goBack()" class="btn btn-secondary">Go Back</button>
          <button (click)="goHome()" class="btn btn-primary">Go Home</button>
          <button (click)="logout()" class="btn btn-danger">Logout</button>
        </div>
      </div>
    </div>
  `,
    styles: [`
    .unauthorized-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
    }

    .unauthorized-content {
      background: white;
      padding: 60px 40px;
      border-radius: 20px;
      box-shadow: 0 20px 60px rgba(0,0,0,0.3);
      text-align: center;
      max-width: 500px;
      width: 100%;
    }

    .icon {
      font-size: 80px;
      margin-bottom: 20px;
      animation: shake 0.5s;
    }

    @keyframes shake {
      0%, 100% { transform: translateX(0); }
      25% { transform: translateX(-10px); }
      75% { transform: translateX(10px); }
    }

    h1 {
      color: #333;
      margin-bottom: 15px;
      font-size: 32px;
    }

    p {
      color: #666;
      margin-bottom: 10px;
      font-size: 16px;
    }

    .details {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 8px;
      margin: 20px 0;
      font-size: 14px;
    }

    .details strong {
      color: #667eea;
    }

    .actions {
      display: flex;
      gap: 10px;
      justify-content: center;
      margin-top: 30px;
      flex-wrap: wrap;
    }

    .btn {
      padding: 12px 30px;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      font-size: 16px;
      font-weight: 500;
      transition: all 0.3s;
    }

    .btn-primary {
      background: #667eea;
      color: white;
    }

    .btn-primary:hover {
      background: #5568d3;
      transform: translateY(-2px);
      box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
    }

    .btn-secondary {
      background: #6c757d;
      color: white;
    }

    .btn-secondary:hover {
      background: #5a6268;
      transform: translateY(-2px);
    }

    .btn-danger {
      background: #dc3545;
      color: white;
    }

    .btn-danger:hover {
      background: #c82333;
      transform: translateY(-2px);
    }
  `]
})
export class UnauthorizedComponent {
    private router = inject(Router);
    private authService = inject(AuthService);

    get userRoles(): string {
        return this.authService.getUserRoles().join(', ') || 'No roles assigned';
    }

    goBack(): void {
        window.history.back();
    }

    goHome(): void {
        const defaultRoute = this.authService.getDefaultRoute();
        this.router.navigate([defaultRoute]);
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
