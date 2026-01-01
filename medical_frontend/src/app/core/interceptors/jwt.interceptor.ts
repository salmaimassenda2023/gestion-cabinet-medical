import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
    console.log('🔄 JWT Interceptor - URL:', req.url, 'Method:', req.method);

    // const authService = inject(AuthService); // avoid circular dependency
    const token = localStorage.getItem('auth_token');

    // Define public endpoints (no token needed)
    const publicEndpoints = [
        { method: 'POST', pattern: /\/api\/cabinet$/ },
        { method: 'POST', pattern: /\/api\/utilisateur\/users\/register\/medecin$/ },
        { method: 'POST', pattern: /\/api\/utilisateur\/users\/bootstrap/ },
        { method: 'PATCH', pattern: /\/api\/utilisateur\/users\/\d+\/cabinet$/ },
        { method: 'POST', pattern: /protocol\/openid-connect\/token/ },
    ];

    // Check if this is a public endpoint
    const isPublic = publicEndpoints.some(endpoint =>
        req.method === endpoint.method && endpoint.pattern.test(req.url)
    );

    if (isPublic) {
        console.log('⏭️ Skipping token for public endpoint:', req.url);
        return next(req);
    }

    // Add token only for protected endpoints
    if (token) {
        console.log('✅ Adding token for protected endpoint');
        const authReq = req.clone({
            setHeaders: { Authorization: `Bearer ${token}` }
        });
        return next(authReq);
    }

    console.warn('⚠️ No token available for request to:', req.url);
    return next(req);
};