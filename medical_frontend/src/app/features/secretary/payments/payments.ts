import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaiementService } from '../../../core/services/paiement.service';
import { ConsultationService } from '../../../core/services/consultation.service';
import { PatientService } from '../../../core/services/patient.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { CabinetService } from '../../doctor/services/cabinet.service';
import { ModalComponent } from "../../../shared/components/modal/modal.component";
import { PaymentFormComponent } from "./payment-bill/payment-bill";
import { switchMap, catchError, tap, finalize } from 'rxjs/operators';
import { of, Subject } from 'rxjs';

@Component({
    selector: 'app-payments',
    templateUrl: './payments.html',
    styleUrls: ['./payments.css'],
    imports: [ModalComponent, CommonModule, FormsModule, PaymentFormComponent]
})
export class SecretaryPaymentComponent implements OnInit, OnDestroy {
    // Data arrays
    payments: any[] = [];
    filteredPayments: any[] = [];

    // Current cabinet ID
    currentCabinetId: number | null = null;

    // Filters
    selectedStatus: string = 'ALL';
    searchTerm: string = '';

    // UI states
    isLoading: boolean = true;
    isProcessing: boolean = false;
    isPaymentFormOpen: boolean = false;
    isBillModalOpen: boolean = false;
    isDeleteModalOpen: boolean = false;
    dataLoaded: boolean = false;
    errorMessage: string = '';

    // Current items
    currentBill: any = null;
    billToDelete: any = null;

    // Statistics
    stats = {
        total: 0,
        pending: 0,
        paid: 0,
        cancelled: 0,
        totalAmount: 0,
        pendingAmount: 0,
        paidAmount: 0
    };

    private destroy$ = new Subject<void>();

    constructor(
        private paiementService: PaiementService,
        private consultationService: ConsultationService,
        private patientService: PatientService,
        private utilisateurService: UtilisateurService,
        private cabinetService: CabinetService,
        private cdr: ChangeDetectorRef
    ) { 
        console.log('🔨 SecretaryPaymentComponent constructor called');
    }

    ngOnInit(): void {
        console.log('SecretaryPaymentComponent initialized');
        this.loadData();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    
loadData(): void {
    console.log('🚀 loadData() called for payments');
    this.isLoading = true;
    this.errorMessage = '';
    
    // First get current user
    this.utilisateurService.getCurrentUser().pipe(
        switchMap(user => {
            console.log('👤 Current user:', user);
            
            if (!user?.idUtilisateur) {
                throw new Error('No user ID found');
            }
            
            // Use getCabinetByMedecinId() with current user ID
            return this.cabinetService.getCabinetByMedecinId(user.idUtilisateur);
        }),
        tap(cabinet => {
            console.log('📦 Cabinet loaded:', cabinet);
            
            if (!cabinet?.id) {
                throw new Error('No cabinet found for this user.');
            }
            
            this.currentCabinetId = cabinet.id;
            console.log('✅ Cabinet ID set:', this.currentCabinetId);
            localStorage.setItem('cabinetId', cabinet.id.toString());
        }),
        switchMap(() => {
            if (!this.currentCabinetId) {
                throw new Error('Cabinet ID not available');
            }
            console.log('📊 Loading payments for cabinet:', this.currentCabinetId);
            return this.paiementService.getFacturesByCabinet(this.currentCabinetId);
        }),
        catchError((error) => {
            console.error('❌ Error loading data:', error);
            this.errorMessage = error.message || 'Error loading payments data';
            
            // Fallback to localStorage as backup
            const storedCabinetId = localStorage.getItem('cabinetId');
            if (storedCabinetId) {
                console.log('🔄 Trying fallback with stored cabinet ID:', storedCabinetId);
                this.currentCabinetId = parseInt(storedCabinetId, 10);
                return this.paiementService.getFacturesByCabinet(this.currentCabinetId!);
            }
            
            return of([]);
        }),
        finalize(() => {
            this.isLoading = false;
            this.cdr.detectChanges();
            console.log('🔄 Change detection triggered');
        })
    ).subscribe({
        next: (data) => {
            console.log('✅ Payments data received:', data);
            this.payments = data || [];
            this.filteredPayments = [...this.payments];
            this.calculateStats();
            this.dataLoaded = true;
            console.log(`✅ ${this.payments.length} payments loaded successfully`);
        },
        error: (error) => {
            console.error('❌ Subscription error:', error);
            this.errorMessage = 'Failed to load payments. Please try again.';
            this.payments = [];
            this.filteredPayments = [];
            this.calculateStats();
        }
    });
}
    // Utility method to get cabinet ID safely
    private getCabinetId(): number | null {
        if (this.currentCabinetId) {
            return this.currentCabinetId;
        }
        
        const storedCabinetId = localStorage.getItem('cabinetId');
        if (storedCabinetId) {
            return parseInt(storedCabinetId, 10);
        }
        
        return null;
    }

    calculateStats(): void {
        this.stats = {
            total: this.payments.length,
            pending: this.payments.filter(p => p.statut === 'EN_ATTENTE').length,
            paid: this.payments.filter(p => p.statut === 'PAYEE').length,
            cancelled: this.payments.filter(p => p.statut === 'ANNULEE').length,
            totalAmount: this.payments.reduce((sum, p) => sum + (p.montantTotal || 0), 0),
            pendingAmount: this.payments
                .filter(p => p.statut === 'EN_ATTENTE')
                .reduce((sum, p) => sum + (p.montantTotal || 0), 0),
            paidAmount: this.payments
                .filter(p => p.statut === 'PAYEE')
                .reduce((sum, p) => sum + (p.montantTotal || 0), 0)
        };
    }

    // Filter methods
    filterByStatus(status: string): void {
        this.selectedStatus = status;
        this.applyFilters();
    }

    onSearchChange(): void {
        this.applyFilters();
    }

    applyFilters(): void {
        let filtered = [...this.payments];

        // Apply status filter
        if (this.selectedStatus !== 'ALL') {
            filtered = filtered.filter(p => p.statut === this.selectedStatus);
        }

        // Apply search filter
        if (this.searchTerm) {
            const search = this.searchTerm.toLowerCase();
            filtered = filtered.filter(p => 
                p.idFacture?.toString().includes(search) ||
                p.patientName?.toLowerCase().includes(search) ||
                p.montantTotal?.toString().includes(search)
            );
        }

        this.filteredPayments = filtered;
    }

    onPaymentCreated(newPayment: any): void {
        console.log('New payment created:', newPayment);
        
        // Ensure the payment has cabinet ID
        if (!newPayment.idCabinet && this.currentCabinetId) {
            newPayment.idCabinet = this.currentCabinetId;
        }
        
        this.payments.unshift(newPayment);
        this.applyFilters();
        this.calculateStats();
        alert('Invoice created successfully!');
    }

    // Modal handlers
    openPaymentForm(): void {
        console.log('Opening payment form...');
        
        if (!this.currentCabinetId) {
            console.error('❌ Cannot open payment form: No cabinet ID');
            this.errorMessage = 'Cabinet information not available. Please refresh.';
            return;
        }
        
        this.isPaymentFormOpen = true;
    }

    closePaymentForm(): void {
        console.log('Closing payment form...');
        this.isPaymentFormOpen = false;
    }

    // View bill
    viewBill(payment: any): void {
        this.currentBill = payment;
        this.isBillModalOpen = true;
    }

    closeBillModal(): void {
        this.isBillModalOpen = false;
        this.currentBill = null;
    }

    // Download PDF
    downloadPDF(idFacture: number): void {
        console.log('Downloading PDF for invoice:', idFacture);
        this.paiementService.generateFacturePDF(idFacture).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `invoice-${idFacture}.pdf`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            },
            error: (error) => {
                console.error('Error downloading PDF:', error);
                alert('Error downloading invoice PDF.');
            }
        });
    }

    // Update status
    updateStatus(payment: any, newStatus: string): void {
        const statusLabels: { [key: string]: string } = {
            'PAYEE': 'Paid',
            'EN_ATTENTE': 'Pending',
            'ANNULEE': 'Cancelled'
        };

        if (confirm(`Are you sure you want to mark this invoice as ${statusLabels[newStatus] || newStatus}?`)) {
            this.isProcessing = true;
            this.paiementService.updateFactureStatut(payment.idFacture, newStatus)
                .pipe(
                    finalize(() => {
                        this.isProcessing = false;
                        this.cdr.detectChanges();
                    })
                )
                .subscribe({
                next: () => {
                    payment.statut = newStatus;
                    if (this.currentBill?.idFacture === payment.idFacture) {
                        this.currentBill.statut = newStatus;
                    }
                    this.calculateStats();
                    this.applyFilters();
                    alert('Invoice status updated successfully!');
                },
                error: (error) => {
                    console.error('Error updating status:', error);
                    alert('Error updating invoice status.');
                }
            });
        }
    }

    // Delete invoice
    openDeleteModal(payment: any): void {
        this.billToDelete = payment;
        this.isDeleteModalOpen = true;
    }

    closeDeleteModal(): void {
        this.isDeleteModalOpen = false;
        this.billToDelete = null;
    }

    confirmDelete(): void {
        if (!this.billToDelete) return;

        this.isProcessing = true;
        this.paiementService.deleteFacture(this.billToDelete.idFacture)
            .pipe(
                finalize(() => {
                    this.isProcessing = false;
                    this.cdr.detectChanges();
                })
            )
            .subscribe({
            next: () => {
                this.payments = this.payments.filter(p => p.idFacture !== this.billToDelete.idFacture);
                this.applyFilters();
                this.calculateStats();
                this.isDeleteModalOpen = false;
                this.billToDelete = null;
                alert('Invoice deleted successfully!');
            },
            error: (error) => {
                console.error('Error deleting invoice:', error);
                alert('Error deleting invoice.');
                this.isDeleteModalOpen = false;
            }
        });
    }

    // Refresh data
    refreshPayments(): void {
        this.loadData();
    }

    // Export methods
    exportToCSV(): void {
        console.log('Exporting to CSV...');
        alert('Export feature coming soon!');
    }

    // Utility methods
    formatDate(dateString: string): string {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    formatDateTime(dateString: string): string {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getStatusClass(status: string): string {
        const statusMap: { [key: string]: string } = {
            'EN_ATTENTE': 'pending',
            'PAYEE': 'paid',
            'ANNULEE': 'cancelled'
        };
        return statusMap[status] || 'unknown';
    }

    getStatusLabel(status: string): string {
        const labelMap: { [key: string]: string } = {
            'EN_ATTENTE': 'Pending',
            'PAYEE': 'Paid',
            'ANNULEE': 'Cancelled'
        };
        return labelMap[status] || status;
    }

    trackById(index: number, item: any): number {
        return item.idFacture || index;
    }
}