import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';

export interface UtilisateurRequest {
    login: string;
    password?: string;
    nom: string;
    prenom: string;
    numTel: string;
    role: string;
    idCabinet?: number;
}

export interface UtilisateurResponse {
    signature: string;
    idUtilisateur: number;
    login: string;
    nom: string;
    prenom: string;
    numTel: string;
    role: string;
    idCabinet?: number;
    actif: boolean;
    email?: string;
}

@Injectable({
    providedIn: 'root'
})
export class UtilisateurService {
    private readonly baseUrl = environment.apiUrl || 'http://localhost:8222';
    private readonly path = '/api/utilisateur/users';

    constructor(private http: HttpClient) { }

    registerMedecin(request: UtilisateurRequest): Observable<UtilisateurResponse> {
        console.log('📝 Registering medecin...');

        // Clear any tokens that might interfere
        localStorage.removeItem('auth_token');

        const headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        const url = `${this.baseUrl}${this.path}/register/medecin`;
        console.log('🔗 Request URL:', url);
        console.log('📦 Request data:', { ...request, password: '[HIDDEN]' });

        return this.http.post<UtilisateurResponse>(url, request, { headers }).pipe(
            tap(response => {
                console.log('✅ Medecin registered:', response);
                if (response.idUtilisateur) {
                    sessionStorage.setItem('temp_user_id', response.idUtilisateur.toString());
                }
            }),
            catchError(error => {
                console.error('❌ Registration error:', error);
                return throwError(() => error);
            })
        );
    }

    createUtilisateur(request: UtilisateurRequest): Observable<UtilisateurResponse> {
        console.log('📝 Creating user...', request);
        return this.http.post<UtilisateurResponse>(`${this.baseUrl}${this.path}`, request);
    }

    getCurrentUser(): Observable<UtilisateurResponse> {
        return this.http.get<UtilisateurResponse>(`${this.baseUrl}${this.path}/me`);
    }

    getUtilisateurById(id: number): Observable<UtilisateurResponse> {
        return this.http.get<UtilisateurResponse>(`${this.baseUrl}${this.path}/${id}`);
    }

    getUtilisateursByCabinet(idCabinet: number): Observable<UtilisateurResponse[]> {
        return this.http.get<UtilisateurResponse[]>(`${this.baseUrl}${this.path}/cabinet/${idCabinet}`);
    }

    updateUtilisateur(id: number, request: any): Observable<UtilisateurResponse> {
        return this.http.put<UtilisateurResponse>(`${this.baseUrl}${this.path}/${id}`, request);
    }

    getAllUsers(): Observable<UtilisateurResponse[]> {
        return this.http.get<UtilisateurResponse[]>(`${this.baseUrl}${this.path}`);
    }

    updateUserStatus(id: number, status: boolean): Observable<UtilisateurResponse> {
        return this.http.put<UtilisateurResponse>(`${this.baseUrl}${this.path}/${id}/status`, { actif: status });
    }

    deleteUser(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}${this.path}/${id}`);
    }
}