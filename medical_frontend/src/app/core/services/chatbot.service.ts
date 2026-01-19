import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { CabinetSearchResult, SearchCabinetsParams, DisponibiliteResponse } from '../../features/doctor/services/cabinet.service';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private readonly baseUrl = environment.apiUrl || 'http://localhost:8222';

  constructor(private http: HttpClient) { }

  searchCabinets(params: SearchCabinetsParams): Observable<CabinetSearchResult[]> {
    let httpParams = new HttpParams();
    
    if (params.specialite) httpParams = httpParams.append('specialite', params.specialite);
    if (params.nom) httpParams = httpParams.append('nom', params.nom);
    if (params.adresse) httpParams = httpParams.append('adresse', params.adresse);
    if (params.prixMax) httpParams = httpParams.append('prixMax', params.prixMax.toString());
    if (params.date) httpParams = httpParams.append('date', params.date);

    return this.http.get<CabinetSearchResult[]>(`${this.baseUrl}/api/cabinet/search`, { params: httpParams });
  }

  // Check availability for a specific cabinet
  checkDisponibilite(cabinetId: number, date: string): Observable<DisponibiliteResponse> {
    return this.http.get<DisponibiliteResponse>(
      `${this.baseUrl}/api/cabinet/${cabinetId}/disponibilite?date=${date}`
    );
  }

  // Get all specialties for suggestions
  getSpecialites(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/api/cabinet/specialites`);
  }

  // Get cabinet services with prices
  getCabinetServices(cabinetId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/api/cabinet/${cabinetId}/services`);
  }

  // Simple keyword-based FAQ responses (can be expanded)
  getBotResponse(message: string): Observable<string> {
    const lowerMsg = message.toLowerCase();
    
    // Simple keyword matching
    if (lowerMsg.includes('price') || lowerMsg.includes('cost') || lowerMsg.includes('tarif')) {
      return of("I can help you find clinics with their prices. You can search by specialty or location. What type of doctor are you looking for?");
    }
    
    if (lowerMsg.includes('availability') || lowerMsg.includes('available') || lowerMsg.includes('time')) {
      return of("I can check available time slots for clinics. Please specify a date (e.g., 'tomorrow' or '2024-12-25') and the type of specialist you need.");
    }
    
    if (lowerMsg.includes('clinic') || lowerMsg.includes('doctor') || lowerMsg.includes('find')) {
      return of("I can help you find clinics near you. What specialty are you looking for? (e.g., cardiology, dermatology, general practitioner)");
    }
    
    return of("I can help you with: 1) Finding clinics by specialty/location, 2) Checking availability, 3) Viewing prices. What would you like to do?");
  }
}