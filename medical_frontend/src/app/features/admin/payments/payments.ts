import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { PaymentTableComponent, Payment } from '../../../shared/components/payment-table/payment-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

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
    payments: Payment[] = [
        {
            id: 'P-001',
            clinicLogo: 'assets/clinic-1.png',
            clinicName: 'St. Mary Medical Center',
            creationDate: '2025-01-10',
            paymentDelay: '5 Days',
            pricingType: 'monthly',
            status: 'active'
        },
        {
            id: 'P-002',
            clinicLogo: 'assets/clinic-2.png',
            clinicName: 'Oakcrest Family Clinic',
            creationDate: '2024-12-15',
            paymentDelay: '0 Days',
            pricingType: 'yearly',
            status: 'active'
        },
        {
            id: 'P-003',
            clinicLogo: 'assets/clinic-3.png',
            clinicName: 'Peak Vision Center',
            creationDate: '2024-11-20',
            paymentDelay: 'Overdue',
            pricingType: 'monthly',
            status: 'expired'
        }
    ];

    activeFilter: 'all' | 'active' | 'expired' = 'all';
    searchTerm: string = '';
    isLogoutModalOpen = false;

    ngOnInit(): void { }

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
