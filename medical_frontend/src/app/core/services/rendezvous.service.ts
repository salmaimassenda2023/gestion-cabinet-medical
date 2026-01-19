import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, map, Observable, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { RendezVous, CreateRendezVousDTO, UpdateRendezVousDTO, StatutRendezVous } from '../models/rendezvous.model';

@Injectable({
    providedIn: 'root'
})

export class RendezvousService {
    private readonly baseUrl = environment.apiUrl || 'http://localhost:8222';
    private readonly path = '/api/rendezvous';

    constructor(private http: HttpClient) { }

    createRendezVous(dto: CreateRendezVousDTO): Observable<RendezVous> {
        return this.http.post<RendezVous>(`${this.baseUrl}${this.path}`, dto);
    }

    getRendezVous(id: number): Observable<RendezVous> {
        return this.http.get<RendezVous>(`${this.baseUrl}${this.path}/${id}`);
    }

    updateRendezVous(id: number, dto: UpdateRendezVousDTO): Observable<RendezVous> {
        return this.http.put<RendezVous>(`${this.baseUrl}${this.path}/${id}`, dto);
    }

    deleteRendezVous(id: number): Observable<void> {
        return this.http.delete<void>(`${this.baseUrl}${this.path}/${id}`);
    }

    changeStatut(id: number, statut: StatutRendezVous): Observable<RendezVous> {
        return this.http.put<RendezVous>(`${this.baseUrl}${this.path}/${id}/statut`, { statut });
    }

    getRendezVousByPatient(patientId: number): Observable<RendezVous[]> {
        return this.http.get<RendezVous[]>(`${this.baseUrl}${this.path}/patient/${patientId}`);
    }

    getRendezVousByMedecinAndDate(medecinId: number, date: string): Observable<RendezVous[]> {
        return this.http.get<RendezVous[]>(`${this.baseUrl}${this.path}/medecin/${medecinId}/date/${date}`);
    }

    getRendezVousDuJour(medecinId: number): Observable<RendezVous[]> {
        return this.http.get<RendezVous[]>(`${this.baseUrl}${this.path}/medecin/${medecinId}/aujourdhui`);
    }

    getListeAttente(medecinId: number): Observable<RendezVous[]> {
        return this.http.get<RendezVous[]>(`${this.baseUrl}${this.path}/liste-attente/medecin/${medecinId}`);
    }

    ajouterEnListeAttente(id: number): Observable<RendezVous> {
        return this.http.post<RendezVous>(`${this.baseUrl}${this.path}/${id}/ajouter-attente`, {});
    }

    retirerDeListeAttente(id: number): Observable<RendezVous> {
        return this.http.post<RendezVous>(`${this.baseUrl}${this.path}/${id}/retirer-attente`, {});
    }

    getPatientSuivant(medecinId: number): Observable<RendezVous | null> {
        return this.http.get<RendezVous>(`${this.baseUrl}${this.path}/liste-attente/suivant/${medecinId}`).pipe(
            catchError(error => {
                console.warn('⚠️ No patient in waiting list or endpoint error:', error.status);
                
                if (error.status === 400 || error.status === 404) {
                    console.log('ℹ️ No patient currently in consultation waiting list');
                    return of(null); 
                }
                
                return throwError(() => error);
            })
        );
    }

    getFirstPatientFromWaitingList(medecinId: number): Observable<RendezVous | null> {
        return this.getListeAttente(medecinId).pipe(
            map(waitingList => {
                // Filter for patients with PRESENT status
                const presentPatients = waitingList.filter(rdv => 
                    rdv.statut === StatutRendezVous.PRESENT
                );
                
                if (presentPatients.length > 0) {
                    // Sort by ordrePassage if available
                    const sortedPatients = presentPatients.sort((a, b) => 
                        (a.ordrePassage || 0) - (b.ordrePassage || 0)
                    );
                    return sortedPatients[0];
                }
                
                return null;
            }),
            catchError(error => {
                console.error('Error getting waiting list:', error);
                return of(null);
            })
        );
    }
}
