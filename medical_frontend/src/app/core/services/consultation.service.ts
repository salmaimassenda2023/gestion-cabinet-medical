import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class ConsultationService {
    private apiUrl = `${environment.apiUrl}/consultation`;

    constructor(private http: HttpClient) { }

    createConsultation(consultation: any): Observable<any> {
        return this.http.post<any>(this.apiUrl, consultation);
    }

    getConsultation(id: number): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/${id}`);
    }

    updateConsultation(id: number, consultation: any): Observable<any> {
        return this.http.put<any>(`${this.apiUrl}/${id}`, consultation);
    }

    deleteConsultation(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    getConsultationsByPatient(idPatient: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/patient/${idPatient}`);
    }

    searchConsultations(params: any): Observable<any[]> {
        return this.http.get<any[]>(`${this.apiUrl}/search`, { params });
    }

    // Clinical Exams
    addExamenClinique(idConsultation: number, examen: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idConsultation}/examens`, examen);
    }

    // Prescriptions - Medications
    createOrdonnanceMedicament(idConsultation: number, ordonnance: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idConsultation}/ordonnances/medicaments`, ordonnance);
    }

    downloadOrdonnanceMedicamentPDF(idConsultation: number, idOrdonnance: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/${idConsultation}/ordonnances/medicaments/${idOrdonnance}/pdf`, { responseType: 'blob' });
    }

    // Prescriptions - Exams
    createOrdonnanceExamen(idConsultation: number, ordonnance: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idConsultation}/ordonnances/examens`, ordonnance);
    }

    downloadOrdonnanceExamenPDF(idConsultation: number, idOrdonnance: number): Observable<Blob> {
        return this.http.get(`${this.apiUrl}/${idConsultation}/ordonnances/examens/${idOrdonnance}/pdf`, { responseType: 'blob' });
    }

    // Invoices (already in PaiementService but re-exposing here if needed)
    createFacture(idConsultation: number, facture: any): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/${idConsultation}/factures`, facture);
    }
}
