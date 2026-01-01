import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { PaymentTableComponent, Payment } from '../../../shared/components/payment-table/payment-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { CabinetService, PaymentResponse } from '../../doctor/services/cabinet.service';

@Component({
    selector: 'app-payments',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        HeaderComponent,
        SidebarComponent,
        PaymentTableComponent,
        ModalComponent
    ],
    templateUrl: './payments.html',
    styleUrls: ['./payments.css']
})
export class PaymentsComponent implements OnInit {
    payments: Payment[] = []; // Initialize as empty, data will be loaded

    activeFilter: 'all' | 'active' | 'expired' = 'all';
    searchTerm: string = '';
    isLogoutModalOpen = false;

    constructor(private cabinetService: CabinetService) { }

    ngOnInit(): void {
        this.loadPayments();
    }

    loadPayments() {
        this.cabinetService.getAllPayments().subscribe({
            next: (data: PaymentResponse[]) => {
                this.payments = data.map((p: PaymentResponse) => ({
                    id: `P-${p.idPaiement}`,
                    clinicLogo: p.cabinetLogo || 'assets/clinic-1.png',
                    clinicName: p.cabinetNom || 'Unknown Clinic',
                    creationDate: new Date(p.datePaiement).toLocaleDateString(),
                    paymentDelay: 'On Time', // Logic to be refined if needed
                    pricingType: 'Subscription', // Logic to derive from amount
                    status: p.statut.toLowerCase() as 'active' | 'expired'
                }));
            },
            error: (err: any) => console.error('Error fetching payments:', err)
        });
    }

    get filteredPayments(): Payment[] {
        let filtered = this.payments;

        // Status filter
        if (this.activeFilter !== 'all') {
            filtered = filtered.filter(p => p.status === this.activeFilter);
        }

        // Search filter
        if (this.searchTerm) {
            const term = this.searchTerm.toLowerCase();
            filtered = filtered.filter(p =>
                p.clinicName.toLowerCase().includes(term) ||
                p.id.toLowerCase().includes(term) ||
                p.creationDate.toLowerCase().includes(term) ||
                p.paymentDelay.toLowerCase().includes(term)
            );
        }

        return filtered;
    }

    onSearch(term: string): void {
        this.searchTerm = term;
    }

    onFilterChange(filter: 'all' | 'active' | 'expired'): void {
        this.activeFilter = filter;
    }

    logout(): void {
        this.isLogoutModalOpen = true;
    }

    confirmLogout(): void {
        console.log('Logging out...');
        this.isLogoutModalOpen = false;
    }
}
