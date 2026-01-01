import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

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
        
        // Create URLSearchParams correctly
        const body = new URLSearchParams();
        body.set('grant_type', 'password');
        body.set('client_id', environment.keycloak.clientId);
        body.set('username', username);
        body.set('password', password);
        body.set('scope', 'openid profile email');
        
        // IMPORTANT: Add client secret if your client is confidential
        // Check in Keycloak if client is "confidential" or "public"
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
        
        // Optional: Call Keycloak logout endpoint
        const logoutUrl = `${environment.keycloak.url}/realms/${environment.keycloak.realm}/protocol/openid-connect/logout`;
        // You can redirect to this URL or call it
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    // Optional: Refresh token method
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
}