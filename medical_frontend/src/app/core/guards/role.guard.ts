import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  console.log('🔒 Role Guard - Checking roles for:', state.url);

  if (!authService.isAuthenticated()) {
    console.log('❌ Role Guard - User not authenticated');
    router.navigate(['/login']);
    return false;
  }

  const requiredRoles = route.data['roles'] as string[];
  
  if (!requiredRoles || requiredRoles.length === 0) {
    console.log('✅ Role Guard - No specific roles required');
    return true;
  }

  const userRoles = authService.getUserRoles();
  console.log('🔒 Role Guard - Required roles:', requiredRoles);
  console.log('🔒 Role Guard - User roles:', userRoles);

  const hasRole = authService.hasAnyRole(requiredRoles);

  if (hasRole) {
    console.log('✅ Role Guard - Access granted');
    return true;
  }

  console.warn('❌ Role Guard - Access denied. Redirecting to unauthorized page');
  router.navigate(['/unauthorized']);
  return false;
};