import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private tokenKey = 'auth_token';
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

        const headers = new HttpHeaders({
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        return this.http.post(url, body.toString(), { headers }).pipe(
            tap((response: any) => {
                if (response.access_token) {
                    this.setToken(response.access_token);
                }
            })
        );
    }

    setToken(token: string): void {
        localStorage.setItem(this.tokenKey, token);
        this.currentUserSubject.next({ token });
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        this.currentUserSubject.next(null);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}
