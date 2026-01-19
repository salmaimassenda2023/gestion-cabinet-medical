import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class PaiementService {
    private apiUrl = `${environment.apiUrl}/api`;

    constructor(private http: HttpClient) { }

    /**
     * Get all factures for a specific cabinet (PRIMARY METHOD)
     * This is the recommended method for multi-cabinet systems
     */
    getFacturesByCabinet(cabinetId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/consultation/cabinet/${cabinetId}/factures`);
    }

    /**
     * Get all factures across all cabinets (ADMIN ONLY)
     * Use this only for admin dashboards or reports
     */
    getAllFactures(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/consultation/factures/all`);
    }

    /**
     * Get factures for a specific consultation
     */
    getFacturesByConsultation(consultationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/consultation/${consultationId}/factures`);
    }

    /**
     * Create a new facture for a consultation
     */
    createFacture(consultationId: number, factureData: any): Observable<any> {
        return this.http.post<any>(
            `${this.apiUrl}/consultation/${consultationId}/factures`,
            factureData
        );
    }

    /**
     * Update facture status (EN_ATTENTE, PAYEE, ANNULEE)
     */
    updateFactureStatut(idFacture: number, statut: string): Observable<any> {
        return this.http.patch<any>(
            `${this.apiUrl}/consultation/factures/${idFacture}/statut`,
            null,
            { params: { statut } }
        );
    }

    /**
     * Delete a facture
     */
    deleteFacture(idFacture: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/consultation/factures/${idFacture}`);
    }

    /**
     * Generate and download facture PDF
     */
    generateFacturePDF(idFacture: number): Observable<Blob> {
        return this.http.get(
            `${this.apiUrl}/consultation/factures/${idFacture}/pdf`,
            { responseType: 'blob' }
        );
    }

  
    /**
     * Get terminated consultations (ready for invoicing)
     */
    getTerminatedConsultations(): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/consultation/terminated`);
    }

    /**
     * Get consultation details including patient info
     */
    getConsultationWithDetails(consultationId: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/consultation/${consultationId}`);
    }

  
    /**
     * Get all active cabinet services
     */
    getCabinetServices(cabinetId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/cabinet/${cabinetId}/services`);
    }

    /**
     * Get services for a specific cabinet
     */
    getServicesByCabinet(cabinetId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/cabinet/${cabinetId}/services`);
    }
}