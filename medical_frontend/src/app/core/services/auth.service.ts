
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { jwtDecode } from 'jwt-decode';

interface DecodedToken {
  sub: string;
  preferred_username: string;
  realm_access: {
    roles: string[];
  };
  exp: number;
  iat: number;
}

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private tokenKey = 'auth_token';
    private refreshTokenKey = 'refresh_token';
    private currentUserSubject = new BehaviorSubject<any>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(private http: HttpClient) {
        const savedToken = localStorage.getItem(this.tokenKey);
        if (savedToken) {
            this.currentUserSubject.next({ token: savedToken });
        }
    }

    login(username: string, password: string): Observable<any> {
        const url = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/protocol/openid-connect/token`;
        
        const body = new URLSearchParams();
        body.set('grant_type', 'password');
        body.set('client_id', environment.keycloak.clientId);
        body.set('username', username);
        body.set('password', password);
        body.set('scope', 'openid profile email');
        
        if (environment.keycloak.clientSecret) {
            body.set('client_secret', environment.keycloak.clientSecret);
        }

        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        console.log('AuthService - Login request details:', {
            url: url,
            clientId: environment.keycloak.clientId,
            hasClientSecret: !!environment.keycloak.clientSecret,
            body: body.toString()
        });

        return this.http.post(url, body.toString(), { headers }).pipe(
            tap((response: any) => {
                console.log('AuthService - Login successful, response:', {
                    hasAccessToken: !!response.access_token,
                    hasRefreshToken: !!response.refresh_token,
                    tokenType: response.token_type,
                    expiresIn: response.expires_in
                });
                
                if (response.access_token) {
                    this.setTokens(response);
                }
            }),
            catchError((error: HttpErrorResponse) => {
                console.error('AuthService - Login error details:', {
                    status: error.status,
                    statusText: error.statusText,
                    error: error.error,
                    headers: error.headers
                });
                return throwError(() => error);
            })
        );
    }

    private setTokens(response: any): void {
        if (response.access_token) {
            localStorage.setItem(this.tokenKey, response.access_token);
        }
        if (response.refresh_token) {
            localStorage.setItem(this.refreshTokenKey, response.refresh_token);
        }
        this.currentUserSubject.next({ 
            token: response.access_token,
            refreshToken: response.refresh_token 
        });
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    getRefreshToken(): string | null {
        return localStorage.getItem(this.refreshTokenKey);
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.refreshTokenKey);
        this.currentUserSubject.next(null);
        
        const logoutUrl = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/protocol/openid-connect/logout`;
    }

    isAuthenticated(): boolean {
        const token = this.getToken();
        if (!token) {
            return false;
        }
        return !this.isTokenExpired();
    }

    refreshToken(): Observable<any> {
        const refreshToken = this.getRefreshToken();
        if (!refreshToken) {
            return throwError(() => new Error('No refresh token available'));
        }

        const url = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/protocol/openid-connect/token`;
        const body = new URLSearchParams();
        body.set('grant_type', 'refresh_token');
        body.set('client_id', environment.keycloak.clientId);
        body.set('refresh_token', refreshToken);
        
        if (environment.keycloak.clientSecret) {
            body.set('client_secret', environment.keycloak.clientSecret);
        }

        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        return this.http.post(url, body.toString(), { headers }).pipe(
            tap((response: any) => {
                if (response.access_token) {
                    this.setTokens(response);
                }
            })
        );
    }

    getDecodedToken(): DecodedToken | null {
        const token = this.getToken();
        if (!token) {
            return null;
        }

        try {
            return jwtDecode<DecodedToken>(token);
        } catch (error) {
            console.error('Error decoding token:', error);
            return null;
        }
    }

    /**
     * Check if token is expired
     */
    isTokenExpired(): boolean {
        const decoded = this.getDecodedToken();
        if (!decoded) {
            return true;
        }

        const currentTime = Date.now() / 1000;
        return decoded.exp < currentTime;
    }

    /**
     * Get user roles from JWT token
     */
    getUserRoles(): string[] {
        const decoded = this.getDecodedToken();
        return decoded?.realm_access?.roles || [];
    }

    /**
     * Check if user has a specific role
     */
    hasRole(role: string): boolean {
        const roles = this.getUserRoles();
        return roles.includes(role);
    }

    /**
     * Check if user has any of the specified roles
     */
    hasAnyRole(roles: string[]): boolean {
        const userRoles = this.getUserRoles();
        return roles.some(role => userRoles.includes(role));
    }

    /**
     * Check if user has all of the specified roles
     */
    hasAllRoles(roles: string[]): boolean {
        const userRoles = this.getUserRoles();
        return roles.every(role => userRoles.includes(role));
    }

    /**
     * Get current username from token
     */
    getUsername(): string | null {
        const decoded = this.getDecodedToken();
        return decoded?.preferred_username || null;
    }

    /**
     * Get default route based on user role
     */
    getDefaultRoute(): string {
        const roles = this.getUserRoles();

        if (roles.includes('SUPER_ADMIN') || roles.includes('ADMIN')) {
            return '/admin';
        } else if (roles.includes('MEDECIN')) {
            return '/doctor';
        } else if (roles.includes('SECRETAIRE')) {
            return '/secretary';
        }
        return '/';
    }
}