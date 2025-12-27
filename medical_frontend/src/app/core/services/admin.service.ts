import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { ApiService } from '../../core/services/api.service';

export interface DashboardStats {
    clinics: number;
    revenue: number;
    medicaments: number;
}

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly path = '/api/admin';

    constructor(private apiService: ApiService) { }

    getDashboardStats(): Observable<DashboardStats> {
        // Mocking for now as specific endpoint might not exist yet
        // In real implementation: return this.apiService.get<DashboardStats>(`${this.path}/stats`);
        return of({
            clinics: 120,
            revenue: 43000,
            medicaments: 570
        });
    }

    getIncomeData(): Observable<any[]> {
        return of([
            { name: '5k', value: 45 },
            { name: '10k', value: 40 },
            { name: '15k', value: 48 },
            { name: '20k', value: 85 },
            { name: '25k', value: 55 },
            { name: '30k', value: 40 },
            { name: '35k', value: 35 },
            { name: '40k', value: 50 },
            { name: '45k', value: 70 },
            { name: '50k', value: 55 },
            { name: '55k', value: 45 },
            { name: '60k', value: 38 },
        ]);
    }
}
