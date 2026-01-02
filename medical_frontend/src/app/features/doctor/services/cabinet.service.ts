// cabinet.service.ts (updated)
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { UtilisateurService } from './../../auth/services/utilisateur.service'; 

export interface CabinetResponse {
    id: number;
    nom: string;
    specialite: string;
    adresse: string;
    numTel: string;
    emailContact: string;
    tarifConsultation: number;
    maxPatientsJour: number;
    dureeConsultation: number;
    logo?: string;
}

export interface ServiceConsultationDTO {
    idService?: number;
    nom: string;
    prix: number;
    description?: string;
}

export interface PaymentResponse {
    idPaiement: number;
    datePaiement: string;
    montant: number;
    statut: string;
    cabinetNom: string;
    cabinetLogo: string;
}

export interface CabinetCreateRequest {
    nom: string;
    specialite: string;
    adresse: string;
    tel: string;
    maxPatientsJour: number;
    dureeConsultation: number;
    logo?: string;
    medecinId: number;  // Add medecinId here
    abonnement?: {
        typeAbonnement: string;
        dateDebut: string;
        dateFin: string;
    };
    serviceConsultationGenerale?: {
        nom: string;
        prix: number;
        description?: string;
    };
}

@Injectable({
    providedIn: 'root'
})
export class CabinetService {
    private readonly baseUrl = environment.apiUrl || 'http://localhost:8222';
    private readonly path = '/api/cabinet';

    constructor(
        private http: HttpClient,
        private utilisateurService: UtilisateurService,
    ) { }

    createCabinet(cabinetData: any): Observable<CabinetResponse> {
        console.log('🏥 Creating cabinet...');

        // Get medecin ID from UtilisateurService
        const medecinId = this.utilisateurService.getMedecinId();
        
        if (!medecinId) {
            const error = new Error('No medecin ID found. Please register as a medecin first.');
            console.error('❌', error.message);
            return throwError(() => error);
        }

        // Add medecin ID to cabinet data
        const cabinetWithMedecin: CabinetCreateRequest = {
            ...cabinetData,
            medecinId: medecinId
        };

        const headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        const url = `${this.baseUrl}${this.path}`;
        console.log('🔗 Request URL:', url);
        console.log('📦 Cabinet data with medecin ID:', cabinetWithMedecin);
        console.log('👨‍⚕️ Medecin ID:', medecinId);

        return this.http.post<CabinetResponse>(url, cabinetWithMedecin, { headers }).pipe(
            tap(response => {
                console.log('✅ Cabinet created:', response);
                if (response.id) {
                    // Store cabinet ID
                    localStorage.setItem('current_cabinet_id', response.id.toString());
                    sessionStorage.setItem('temp_cabinet_id', response.id.toString());
                    
                    // Update medecin with cabinet ID
                    this.utilisateurService.updateMedecinWithCabinet(response.id);
                }
            }),
            catchError(error => {
                console.error('❌ Cabinet creation error:', error);
                return throwError(() => error);
            })
        );
    }

    getAllCabinets(): Observable<CabinetResponse[]> {
        return this.http.get<CabinetResponse[]>(`${this.baseUrl}${this.path}`);
    }

    getAllPayments(): Observable<PaymentResponse[]> {
        return this.http.get<PaymentResponse[]>(`${this.baseUrl}/api/cabinet/paiement`);
    }

    getCabinet(id: number): Observable<CabinetResponse> {
        return this.http.get<CabinetResponse>(`${this.baseUrl}${this.path}/${id}`);
    }

    updateCabinet(id: number, cabinet: any): Observable<CabinetResponse> {
        return this.http.put<CabinetResponse>(`${this.baseUrl}${this.path}/${id}`, cabinet);
    }

    deleteCabinet(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}${this.path}/${id}`);
    }

    addService(cabinetId: number, service: ServiceConsultationDTO): Observable<ServiceConsultationDTO> {
        return this.http.post<ServiceConsultationDTO>(`${this.baseUrl}${this.path}/${cabinetId}/services`, service);
    }

    getServices(cabinetId: number): Observable<ServiceConsultationDTO[]> {
        return this.http.get<ServiceConsultationDTO[]>(`${this.baseUrl}${this.path}/${cabinetId}/services`);
    }
}