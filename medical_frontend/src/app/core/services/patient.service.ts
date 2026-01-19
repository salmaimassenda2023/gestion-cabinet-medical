import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { catchError, Observable, of } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Patient } from '../models/patient.model';

@Injectable({
    providedIn: 'root'
})
export class PatientService {
    private readonly baseUrl = environment.apiUrl || 'http://localhost:8222';
    private readonly path = '/api/patient';

    constructor(private http: HttpClient) { }

    getAllPatients(): Observable<Patient[]> {
        return this.http.get<Patient[]>(`${this.baseUrl}${this.path}`);
    }

    getPatientsByCabinet(idCabinet: number): Observable<Patient[]> {
        return this.http.get<Patient[]>(`${this.baseUrl}${this.path}/cabinet/${idCabinet}`);
    }

    getPatientById(id: number): Observable<Patient> {
        return this.http.get<Patient>(`${this.baseUrl}${this.path}/${id}`);
    }

    createPatient(patient: Patient): Observable<Patient> {
        return this.http.post<Patient>(`${this.baseUrl}${this.path}`, patient);
    }

    updatePatient(id: number, patient: any): Observable<Patient> {
        return this.http.put<Patient>(`${this.baseUrl}${this.path}/${id}`, patient);
    }

    deletePatient(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}${this.path}/${id}`);
    }

    searchPatients(searchTerm: string): Observable<Patient[]> {
        return this.http.get<Patient[]>(`${this.baseUrl}${this.path}/search`, {
            params: { searchTerm }
        });
    }

    /**
     * Get patient medical dossier
     */
    getPatientDossier(patientId: number): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}${this.path}/${patientId}/dossier`).pipe(
            catchError(err => {
                console.warn('⚠️ Could not load patient dossier:', err);
                return of(null);
            })
        );
    }

    /**
     * Update patient medical dossier
     */
    updatePatientDossier(patientId: number, dossier: any): Observable<any> {
        return this.http.put<any>(`${this.baseUrl}${this.path}/${patientId}/dossier`, dossier);
    }

    /**
     * Get basic patient info (optimized for notifications)
     */
    getPatientInfo(patientId: number): Observable<any> {
        return this.http.get<any>(`${this.baseUrl}${this.path}/${patientId}/info`).pipe(
            catchError(err => {
                console.warn('⚠️ Could not load patient info:', err);
                return this.getPatientById(patientId);
            })
        );
    }

    /**
     * Get patient documents
     */
    getPatientDocuments(patientId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.baseUrl}${this.path}/${patientId}/documents`).pipe(
            catchError(err => {
                console.warn('Could not load patient documents:', err);
                return of([]);
            })
        );
    }

    /**
     * Upload a document for a patient
     */
    uploadDocument(patientId: number, formData: FormData): Observable<any> {
        return this.http.post<any>(
            `${this.baseUrl}${this.path}/${patientId}/documents`,
            formData
        );
    }

    /**
     * Delete a document
     */
    deleteDocument(documentId: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}${this.path}/documents/${documentId}`);
    }

    /**
     * Download a document
     */
    downloadDocument(documentId: number): Observable<Blob> {
        return this.http.get(`${this.baseUrl}${this.path}/documents/${documentId}/download`, {
            responseType: 'blob'
        });
    }
}