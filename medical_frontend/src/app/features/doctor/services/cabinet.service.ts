import { Injectable } from '@angular/core';
import { forkJoin, Observable, of, throwError } from 'rxjs';
import { catchError, map, switchMap, tap } from 'rxjs/operators';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { environment } from '../../../../environments/environment';
import { UtilisateurService } from './../../auth/services/utilisateur.service';

export interface CabinetResponse {
    abonnement: any;
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
    medecinId?: number;
    medecinNom?: string;
    medecinPrenom?: string;
}

export interface ServiceConsultationDTO {
    idService?: number;
    nom: string;
    prix: number;
    description?: string;
}


export interface CabinetCreateRequest {
    nom: string;
    specialite: string;
    adresse: string;
    tel: string;
    maxPatientsJour: number;
    dureeConsultation: number;
    logo?: string;
    medecinId: number; 
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

export interface SearchCabinetsParams {
    specialite?: string;
    nom?: string;
    adresse?: string;
    prixMax?: number;
    date?: string; 
}

export interface DisponibiliteResponse {
    cabinetId: number;
    cabinetNom: string;
    date: string;
    creneauxDisponibles: string[]; 
    servicePrix: Map<string, number>; 
}

export interface CabinetSearchResult {
    id: number;
    nom: string;
    specialite: string;
    adresse: string;
    numTel: string;
    tarifConsultation: number;
    services: ServiceConsultationDTO[];
    noteMoyenne?: number;
}
export interface AbonnementResponse {
    idAbonnement: number;
    dateDebut: string;
    dateFin: string;
    statut: string;
    montant: number;
    typePeriode: string;
    cabinetNom: string;
    cabinetLogo: string;
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
        return this.http.get<CabinetResponse[]>(`${this.baseUrl}${this.path}`).pipe(
            switchMap(cabinets => {
                // For each cabinet, fetch doctor info if medecinId exists
                const cabinetsWithDoctor = cabinets.map(cabinet => {
                    if (cabinet.medecinId) {
                        return this.utilisateurService.getUtilisateurById(cabinet.medecinId).pipe(
                            map(doctor => ({
                                ...cabinet,
                                medecinNom: doctor.nom,
                                medecinPrenom: doctor.prenom,
                                medecinFullName: `${doctor.prenom} ${doctor.nom}`
                            })),
                            catchError(() => of(cabinet)) 
                        );
                    }
                    return of(cabinet);
                });

                return forkJoin(cabinetsWithDoctor);
            })
        );
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
    getCabinetByMedecinId(medecinId: number): Observable<any> {
        return this.http.get(`${this.baseUrl}${this.path}/medecin/${medecinId}`);
    }
    getAllAbonnements(): Observable<AbonnementResponse[]> {
        return this.http.get<AbonnementResponse[]>(`${this.baseUrl}${this.path}/abonnements`);
    }


    /**
     * Get cabinet ID for the current user (simplified version)
     */
    getCurrentUserCabinetId(): Observable<number> {
        return this.getCabinetForCurrentUser().pipe(
            map(cabinet => {
                if (!cabinet?.id) {
                    throw new Error('No cabinet found for current user');
                }
                return cabinet.id;
            }),
            catchError(error => {
                console.error('Error getting cabinet ID:', error);
                return throwError(() => error);
            })
        );
    }
    /**
     * Get cabinet by any user ID (handles both MEDECIN and SECRETAIRE)
     */
    getCabinetByUserId(userId: number): Observable<CabinetResponse> {
        console.log('👤 Getting cabinet for user ID:', userId);

        return this.http.get<CabinetResponse>(`${this.baseUrl}${this.path}/user/${userId}`).pipe(
            catchError(error => {
                console.warn('⚠️ /user/{userId} endpoint failed:', error.message);
                return this.http.get<CabinetResponse>(`${this.baseUrl}${this.path}/for-user/${userId}`).pipe(
                    catchError(error2 => {
                        console.warn('⚠️ /for-user/{userId} endpoint also failed');

                        return this.getAllCabinets().pipe(
                            map(cabinets => {
                                if (cabinets.length === 0) {
                                    throw new Error('No cabinets found in system');
                                }

                                const cabinet = cabinets[0];
                                console.log('🔄 Using fallback: first cabinet found');
                                return cabinet;
                            })
                        );
                    })
                );
            })
        );
    }

    /**
     * Get cabinet for the current logged-in user
     */
    getCabinetForCurrentUser(): Observable<CabinetResponse> {
        console.log('🏥 Getting cabinet for current user...');

        return this.utilisateurService.getCurrentUser().pipe(
            switchMap(user => {
                if (!user?.idUtilisateur) {
                    return throwError(() => new Error('No user ID found'));
                }

                console.log('👤 Current user role:', user.role);

                return this.getAllCabinets().pipe(
                    map(cabinets => {
                        if (cabinets.length === 0) {
                            throw new Error('No cabinets found in system');
                        }

                        const cabinet = cabinets[0];
                        console.log('✅ Using cabinet:', cabinet.nom, 'ID:', cabinet.id);

                        localStorage.setItem('cabinetId', cabinet.id.toString());
                        localStorage.setItem('currentCabinet', JSON.stringify(cabinet));

                        return cabinet;
                    })
                );
            }),
            catchError(error => {
                console.error('❌ Error getting cabinet:', error);

                const dummyCabinet: CabinetResponse = {
                    id: 1,
                    nom: 'Development Cabinet',
                    specialite: 'General Medicine',
                    adresse: '123 Test Street',
                    numTel: '+212600000000',
                    emailContact: 'dev@example.com',
                    tarifConsultation: 200,
                    maxPatientsJour: 20,
                    dureeConsultation: 30,
                    logo: '',
                    medecinId: 1,
                    medecinNom: 'Dev',
                    medecinPrenom: 'Doctor',
                    abonnement: {
                        idAbonnement: 1,
                        dateDebut: new Date().toISOString(),
                        dateFin: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString(),
                        statut: 'ACTIF',
                        montant: 1000,
                        typePeriode: 'MONTHLY'
                    }
                };

                console.warn('⚠️ Using dummy cabinet for development');
                return of(dummyCabinet);
            })
        );
    }


}