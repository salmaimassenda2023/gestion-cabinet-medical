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
    signature?: string;
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

export interface UtilisateurUpdateRequest {
    nom?: string;
    prenom?: string;
    numTel?: string;
    signature?: string;
    idCabinet?: number;
}

export interface PasswordUpdateRequest {
    oldPassword: string;
    newPassword: string;
}

@Injectable({
    providedIn: 'root'
})
export class UtilisateurService {
    private readonly baseUrl = environment.apiUrl || 'http://localhost:8222';
    private readonly path = '/api/utilisateur/users';

    // Key for storing medecin data
    private readonly MEDECIN_STORAGE_KEY = 'medecin_data';
    private readonly MEDECIN_ID_KEY = 'medecin_id';

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

                // Store medecin ID for cabinet creation
                if (response.idUtilisateur) {
                    this.storeMedecinData(response);
                    sessionStorage.setItem('temp_user_id', response.idUtilisateur.toString());
                }
            }),
            catchError(error => {
                console.error('❌ Registration error:', error);
                return throwError(() => error);
            })
        );
    }

    // Store medecin data in localStorage
    private storeMedecinData(medecin: UtilisateurResponse): void {
        localStorage.setItem(this.MEDECIN_ID_KEY, medecin.idUtilisateur.toString());
        localStorage.setItem(this.MEDECIN_STORAGE_KEY, JSON.stringify(medecin));
        console.log('💾 Medecin data stored:', medecin);
    }

    // Get medecin ID for cabinet creation
    getMedecinId(): number | null {
        const id = localStorage.getItem(this.MEDECIN_ID_KEY);
        return id ? parseInt(id, 10) : null;
    }
    getUtilisateursByRole(role: string): Observable<UtilisateurResponse[]> {
        return this.http.get<UtilisateurResponse[]>(`${this.baseUrl}${this.path}/role/${role}`);
    }

    getMedecinData(): UtilisateurResponse | null {
        const data = localStorage.getItem(this.MEDECIN_STORAGE_KEY);
        return data ? JSON.parse(data) : null;
    }

    clearMedecinData(): void {
        localStorage.removeItem(this.MEDECIN_ID_KEY);
        localStorage.removeItem(this.MEDECIN_STORAGE_KEY);
        sessionStorage.removeItem('temp_user_id');
    }

    updateMedecinWithCabinet(cabinetId: number): void {
        const medecinData = this.getMedecinData();
        if (medecinData) {
            const updatedMedecin = {
                ...medecinData,
                idCabinet: cabinetId
            };
            localStorage.setItem(this.MEDECIN_STORAGE_KEY, JSON.stringify(updatedMedecin));

            this.updateCabinetId(medecinData.idUtilisateur, cabinetId).subscribe({
                next: () => console.log('✅ Medecin updated with cabinet ID'),
                error: (err) => console.error('❌ Failed to update medecin:', err)
            });
        }
    }

    createUtilisateur(request: UtilisateurRequest): Observable<UtilisateurResponse> {
        console.log('📝 Creating user...', request);
        return this.http.post<UtilisateurResponse>(`${this.baseUrl}${this.path}`, request);
    }

    updateCabinetId(userId: number, cabinetId: number): Observable<void> {
        return this.http.put<void>(`${this.baseUrl}${this.path}/${userId}/cabinet?idCabinet=${cabinetId}`, {});
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

    updateUtilisateur(id: number, request: UtilisateurUpdateRequest): Observable<UtilisateurResponse> {
        return this.http.put<UtilisateurResponse>(`${this.baseUrl}${this.path}/${id}`, request);
    }

    changePassword(id: number, request: PasswordUpdateRequest): Observable<void> {
        return this.http.put<void>(`${this.baseUrl}${this.path}/${id}/password`, request);
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