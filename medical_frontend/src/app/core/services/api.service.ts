import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth.service';

@Injectable({
    providedIn: 'root'
})
export class ApiService {
    private readonly baseUrl = environment.apiUrl;
    private authService = inject(AuthService); // Inject AuthService

    constructor(private http: HttpClient) { }

    get<T>(path: string, params: HttpParams = new HttpParams()): Observable<T> {
        const headers = this.createHeaders();
        return this.http.get<T>(`${this.baseUrl}${path}`, { params, headers })
            .pipe(catchError(this.formatErrors));
    }

    put<T>(path: string, body: any = {}): Observable<T> {
        const headers = this.createHeaders();
        return this.http.put<T>(`${this.baseUrl}${path}`, JSON.stringify(body), { headers })
            .pipe(catchError(this.formatErrors));
    }

    post<T>(path: string, body: any = {}): Observable<T> {
        const headers = this.createHeaders();
        return this.http.post<T>(`${this.baseUrl}${path}`, JSON.stringify(body), { headers })
            .pipe(catchError(this.formatErrors));
    }

    delete<T>(path: string): Observable<T> {
        const headers = this.createHeaders();
        return this.http.delete<T>(`${this.baseUrl}${path}`, { headers })
            .pipe(catchError(this.formatErrors));
    }

    private createHeaders(): HttpHeaders {
        const token = this.authService.getToken();
        let headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        if (token) {
            headers = headers.set('Authorization', `Bearer ${token}`);
        }

        return headers;
    }

    private formatErrors(error: HttpErrorResponse) {
        console.error('API Error:', error);
        return throwError(() => error);
    }
}