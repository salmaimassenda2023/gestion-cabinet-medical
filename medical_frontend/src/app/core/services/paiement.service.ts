import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class PaiementService {
    private apiUrl = `${environment.apiUrl}/consultation`;

    constructor(private http: HttpClient) { }

    getFacturesByConsultation(consultationId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/${consultationId}/factures`);
    }

    getFacturesByPatient(patientId: number): Observable<any[]> {
        // This depends on whether there's a dedicated search endpoint for factures across consultations
        // For now, using the search endpoint if applicable or just searching consultations
        return this.http.get<any[]>(`${this.apiUrl}/search?idPatient=${patientId}`);
    }

    createFacture(consultationId: number, factureData: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${consultationId}/factures`, factureData);
    }

    updateFactureStatut(factureId: number, statut: string): Observable<any> {
        return this.http.patch<any>(`${this.apiUrl}/factures/${factureId}/statut?statut=${statut}`, {});
    }

    generateFacturePDF(factureId: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/factures/${factureId}/pdf`, { responseType: 'blob' });
    }

    searchConsultations(idPatient?: number, dateDebut?: string, dateFin?: string): Observable<any[]> {
        let params = '';
        if (idPatient) params += `idPatient=${idPatient}&`;
        if (dateDebut) params += `dateDebut=${dateDebut}&`;
        if (dateFin) params += `dateFin=${dateFin}&`;
        return this.http.get<any[]>(`${this.apiUrl}/search?${params}`);
    }
}
