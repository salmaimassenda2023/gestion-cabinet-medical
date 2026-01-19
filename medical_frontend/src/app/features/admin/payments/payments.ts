import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { PaymentTableComponent, Payment } from '../../../shared/components/payment-table/payment-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { CabinetService, AbonnementResponse } from '../../doctor/services/cabinet.service';

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
    payments: Payment[] = [];
    activeFilter: 'all' | 'active' | 'expired' = 'all';
    searchTerm: string = '';
    isLogoutModalOpen = false;
    isLoading = false;
    errorMessage: string | null = null;

    constructor(private cabinetService: CabinetService) { }

    ngOnInit(): void {
        this.loadAbonnements();
    }

    loadAbonnements() {
        this.isLoading = true;
        this.errorMessage = null;
        
        console.log('Loading abonnements...');
        
        this.cabinetService.getAllAbonnements().subscribe({
            next: (data: AbonnementResponse[]) => {
                console.log('Received abonnements:', data);
                
                if (!data || data.length === 0) {
                    console.warn('No abonnements found');
                    this.payments = [];
                    this.isLoading = false;
                    return;
                }
                
                this.payments = data.map((a: AbonnementResponse) => {
                    // Determine status
                    let status: 'active' | 'expired';
                    const now = new Date();
                    const dateFin = new Date(a.dateFin);
                    
                    if (a.statut?.toLowerCase() === 'actif') {
                        status = dateFin < now ? 'expired' : 'active';
                    }else {
                        status = dateFin < now ? 'expired' : 'active';
                    }
                    
                    // Determine pricing type
                    let pricingType = 'Custom';
                    if (a.typePeriode?.toLowerCase() === 'annuel') {
                        pricingType = 'Annual Subscription';
                    } else if (a.typePeriode?.toLowerCase() === 'mensuel') {
                        pricingType = 'Monthly Subscription';
                    }
                    
                    return {
                        id: `A-${a.idAbonnement}`,
                        clinicLogo: a.cabinetLogo || 'assets/default-clinic.png',
                        clinicName: a.cabinetNom || 'Unknown Clinic',
                        creationDate: this.formatDate(a.dateDebut),
                        expiryDate: this.formatDate(a.dateFin),
                        paymentDelay: this.calculateDelay(a.dateFin),
                        pricingType: pricingType,
                        amount: a.montant,
                        status: status,
                        typePeriode: a.typePeriode
                    };
                });
                
                console.log('Processed abonnements:', this.payments);
                this.isLoading = false;
            },
            error: (err: any) => {
                console.error('Error fetching abonnements:', err);
                this.errorMessage = 'Failed to load subscriptions. Please try again.';
                this.isLoading = false;
                this.payments = [];
            }
        });
    }

    private formatDate(dateStr: string | Date): string {
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (e) {
            console.error('Date formatting error:', e);
            return 'Invalid Date';
        }
    }

    private calculateDelay(expiryDateStr: string): string {
        try {
            const expiryDate = new Date(expiryDateStr);
            const now = new Date();
            const diffTime = expiryDate.getTime() - now.getTime();
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            if (diffDays > 30) {
                return `${Math.floor(diffDays/30)} months remaining`;
            } else if (diffDays > 0) {
                return `${diffDays} days remaining`;
            } else if (diffDays === 0) {
                return 'Expires today';
            } else {
                return `Expired ${Math.abs(diffDays)} days ago`;
            }
        } catch (e) {
            return 'N/A';
        }
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
                p.clinicName?.toLowerCase().includes(term) ||
                p.id?.toLowerCase().includes(term) ||
                p.creationDate?.toLowerCase().includes(term) ||
                p.pricingType?.toLowerCase().includes(term)
            );
        }

        return filtered;
    }

    onSearch(term: string): void {
        this.searchTerm = term;
    }

    
onFilterChange(filter: any): void {
    console.log('Received filter:', filter); 
    
    if (filter === 'suspended') {
        console.log('Converting suspended to active');
        this.activeFilter = 'active'; 
    } else if (['all', 'active', 'expired'].includes(filter)) {
        this.activeFilter = filter;
    } else {
        console.warn('Invalid filter received:', filter);
        this.activeFilter = 'all'; 
    }
    
    console.log('Active filter set to:', this.activeFilter);
}

    refreshPayments(): void {
        this.loadAbonnements();
    }

    logout(): void {
        this.isLogoutModalOpen = true;
    }

    confirmLogout(): void {
        console.log('Logging out...');
        localStorage.clear();
        sessionStorage.clear();
        this.isLogoutModalOpen = false;
        window.location.href = '/login';
    }
}