import { HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req: HttpRequest<any>, next: HttpHandlerFn) => {
    console.log('🔄 JWT Interceptor - URL:', req.url, 'Method:', req.method);

    const token = localStorage.getItem('auth_token');

    // Define public endpoints (no token needed)
    const publicEndpoints = [
        '/api/utilisateur/users/register/medecin',
        '/api/cabinet',
        '/api/utilisateur/users/bootstrap',
        '/protocol/openid-connect/token'
    ];

    // Check if this is a public endpoint using includes (more reliable)
    const isPublic = publicEndpoints.some(endpoint => 
        req.url.includes(endpoint)
    );

    // Also check for PATCH requests to user cabinet
    const isUserCabinetPatch = req.method === 'PATCH' && 
                               req.url.includes('/api/utilisateur/users/') && 
                               req.url.includes('/cabinet');

    if (isPublic || isUserCabinetPatch) {
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