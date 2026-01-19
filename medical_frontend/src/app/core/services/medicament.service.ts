import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Medicament {
    id: string;
    nom: string;
    dosage: string;
    forme: string;
}

@Injectable({
    providedIn: 'root'
})
export class MedicamentService {
    private apiUrl = `${environment.apiUrl}/api/medicament`;

    constructor(private http: HttpClient) { }

    getAllMedicaments(): Observable<Medicament[]> {
        return this.http.get<Medicament[]>(this.apiUrl);
    }

    searchMedicaments(term: string): Observable<Medicament[]> {
        return this.http.get<Medicament[]>(`${this.apiUrl}/search`, { params: { term } });
    }

    autocomplete(prefix: string): Observable<Medicament[]> {
        return this.http.get<Medicament[]>(`${this.apiUrl}/autocomplete`, { params: { prefix } });
    }
}
