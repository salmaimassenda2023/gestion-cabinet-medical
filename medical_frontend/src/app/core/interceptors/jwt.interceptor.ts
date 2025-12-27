import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const token = authService.getToken();

    // Skip adding token for Keycloak requests
    if (req.url.includes('/protocol/openid-connect/token')) {
        return next(req);
    }

    let headers = req.headers;

    if (token) {
        headers = headers.set('Authorization', `Bearer ${token}`);
    }

    // Only set default Content-Type if not already explicitly set
    if (!headers.has('Content-Type') && ['POST', 'PUT', 'PATCH'].includes(req.method)) {
        headers = headers.set('Content-Type', 'application/json');
    }

    const cloned = req.clone({ headers });
    return next(cloned);
};
