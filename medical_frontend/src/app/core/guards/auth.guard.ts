import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('🔒 Auth Guard - Checking authentication for:', state.url);

  if (authService.isAuthenticated()) {
    console.log('✅ Auth Guard - User is authenticated');
    return true;
  }

  console.log('❌ Auth Guard - User not authenticated, redirecting to login');
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};