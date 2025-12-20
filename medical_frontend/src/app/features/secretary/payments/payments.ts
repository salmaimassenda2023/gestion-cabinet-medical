import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { Service, PaymentRecord } from '../../../core/models/payment.model';

@Component({
    selector: 'app-secretary-payments',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './payments.html',
    styleUrls: ['./payments.css']
})
export class SecretaryPaymentsComponent implements OnInit {
    payments: PaymentRecord[] = [
        { id: '1', patientName: 'Peter Mullin', services: [{ id: 's1', name: 'Blood Test', price: 50 }], consultationPrice: 30, totalPrice: 80, date: '2025-12-19', status: 'paid' },
        { id: '2', patientName: 'Sandra Bay', services: [], consultationPrice: 30, totalPrice: 30, date: '2025-12-19', status: 'pending' }
    ];

    availableServices: Service[] = [
        { id: 's1', name: 'Blood Test', price: 50 },
        { id: 's2', name: 'X-Ray', price: 120 },
        { id: 's3', name: 'Vaccination', price: 25 },
        { id: 's4', name: 'MRI', price: 450 }
    ];

    consultationBasePrice = 30;

    isPayModalOpen = false;
    isBillModalOpen = false;

    selectedPatientName = '';
    selectedServices: Service[] = [];
    currentBill?: PaymentRecord;

    ngOnInit() { }

    openPayModal() {
        this.selectedPatientName = '';
        this.selectedServices = [];
        this.isPayModalOpen = true;
    }

    toggleService(service: Service) {
        const index = this.selectedServices.findIndex(s => s.id === service.id);
        if (index > -1) {
            this.selectedServices.splice(index, 1);
        } else {
            this.selectedServices.push(service);
        }
    }

    isServiceSelected(service: Service): boolean {
        return this.selectedServices.some(s => s.id === service.id);
    }

    get currentTotal(): number {
        return this.consultationBasePrice + this.selectedServices.reduce((acc, s) => acc + s.price, 0);
    }

    processPayment() {
        const newPayment: PaymentRecord = {
            id: (this.payments.length + 1).toString(),
            patientName: this.selectedPatientName,
            services: [...this.selectedServices],
            consultationPrice: this.consultationBasePrice,
            totalPrice: this.currentTotal,
            date: new Date().toISOString().split('T')[0],
            status: 'paid'
        };

        this.payments = [newPayment, ...this.payments];
        this.currentBill = newPayment;
        this.isPayModalOpen = false;
        this.isBillModalOpen = true;
    }

    printBill() {
        window.print();
    }
}
