import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
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

    updatePatient(id: number, patient: Patient): Observable<Patient> {
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
}
