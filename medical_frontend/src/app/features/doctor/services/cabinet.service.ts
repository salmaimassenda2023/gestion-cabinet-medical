import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';

export interface CabinetResponse {
    idCabinet: number;
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

@Injectable({
    providedIn: 'root'
})
export class CabinetService {
    private readonly path = '/api/cabinet';

    constructor(private apiService: ApiService) { }

    createCabinet(cabinet: any): Observable<CabinetResponse> {
        return this.apiService.post<CabinetResponse>(this.path, cabinet);
    }

    getAllCabinets(): Observable<CabinetResponse[]> {
        return this.apiService.get<CabinetResponse[]>(this.path);
    }

    getCabinet(id: number): Observable<CabinetResponse> {
        return this.apiService.get<CabinetResponse>(`${this.path}/${id}`);
    }

    updateCabinet(id: number, cabinet: any): Observable<CabinetResponse> {
        return this.apiService.put<CabinetResponse>(`${this.path}/${id}`, cabinet);
    }

    addService(cabinetId: number, service: ServiceConsultationDTO): Observable<ServiceConsultationDTO> {
        return this.apiService.post<ServiceConsultationDTO>(`${this.path}/${cabinetId}/services`, service);
    }

    getServices(cabinetId: number): Observable<ServiceConsultationDTO[]> {
        return this.apiService.get<ServiceConsultationDTO[]>(`${this.path}/${cabinetId}/services`);
    }
}
