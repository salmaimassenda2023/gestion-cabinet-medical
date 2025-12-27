import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

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
    idUtilisateur: number;
    login: string;
    nom: string;
    prenom: string;
    numTel: string;
    role: string;
    idCabinet?: number;
    actif: boolean;
    email?: string; // Add email if missing
}

@Injectable({
    providedIn: 'root'
})
export class UtilisateurService {
    private readonly path = '/api/utilisateur/users';

    constructor(private apiService: ApiService) { }

    registerMedecin(request: UtilisateurRequest): Observable<UtilisateurResponse> {
        return this.apiService.post<UtilisateurResponse>(`${this.path}/register/medecin`, request);
    }

    getCurrentUser(): Observable<UtilisateurResponse> {
        return this.apiService.get<UtilisateurResponse>(`${this.path}/me`);
    }

    getUtilisateurById(id: number): Observable<UtilisateurResponse> {
        return this.apiService.get<UtilisateurResponse>(`${this.path}/${id}`);
    }

    getUtilisateursByCabinet(idCabinet: number): Observable<UtilisateurResponse[]> {
        return this.apiService.get<UtilisateurResponse[]>(`${this.path}/cabinet/${idCabinet}`);
    }

    updateUtilisateur(id: number, request: any): Observable<UtilisateurResponse> {
        return this.apiService.put<UtilisateurResponse>(`${this.path}/${id}`, request);
    }

    getAllUsers(): Observable<UtilisateurResponse[]> {
        return this.apiService.get<UtilisateurResponse[]>(this.path);
    }

    updateUserStatus(id: number, status: boolean): Observable<UtilisateurResponse> {
        return this.apiService.put<UtilisateurResponse>(`${this.path}/${id}/status`, { actif: status });
    }

    deleteUser(id: number): Observable<void> {
        return this.apiService.delete<void>(`${this.path}/${id}`);
    }
}
