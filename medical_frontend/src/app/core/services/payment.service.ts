// src/app/shared/services/payment.service.ts
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';

export interface CreatePaymentIntentRequest {
  amount: number; 
  currency?: string;
  plan: string;
  metadata?: {
    abonnementId?: string;
    cabinetId?: string;
    plan?: string;
  };
}

export interface PaymentIntentResponse {
  clientSecret: string;
  paymentIntentId: string;
}

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly path = '/api/payments';

  constructor(private apiService: ApiService) {}

  createPaymentIntent(request: CreatePaymentIntentRequest): Observable<PaymentIntentResponse> {
    return this.apiService.post<PaymentIntentResponse>(`${this.path}/create-intent`, request);
  }

  confirmPayment(paymentIntentId: string, cabinetId: number): Observable<any> {
    return this.apiService.post(`${this.path}/confirm`, {
      paymentIntentId,
      cabinetId
    });
  }
}