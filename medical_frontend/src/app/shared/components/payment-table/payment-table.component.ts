import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Payment {
    id: string;
    clinicLogo: string;
    clinicName: string;
    creationDate: string;
    paymentDelay: string;
    pricingType: string;
    status: 'active' | 'expired';
}

@Component({
    selector: 'app-payment-table',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './payment-table.component.html',
    styleUrls: ['./payment-table.component.css']
})
export class PaymentTableComponent {
    @Input() payments: Payment[] = [];
    @Input() currentPage: number = 1;
    @Input() totalPages: number = 1;
    @Input() activeFilter: 'all' | 'active' | 'expired' = 'all';

    @Output() pageChange = new EventEmitter<number>();
    @Output() filterChange = new EventEmitter<'all' | 'active' | 'expired'>();

    showFilterMenu = false;

    getStatusClass(status: 'active' | 'expired'): string {
        return status === 'active' ? 'status-active' : 'status-expired';
    }

    getStatusText(status: 'active' | 'expired'): string {
        return status === 'active' ? 'Active' : 'Expired';
    }

    getPricingTypeText(type: string): string {
        if (type === 'monthly') return 'Monthly';
        if (type === 'yearly') return 'Yearly';
        return type;
    }

    previousPage(): void {
        if (this.currentPage > 1) {
            this.pageChange.emit(this.currentPage - 1);
        }
    }

    nextPage(): void {
        if (this.currentPage < this.totalPages) {
            this.pageChange.emit(this.currentPage + 1);
        }
    }

    setFilter(filter: 'all' | 'active' | 'expired'): void {
        this.activeFilter = filter;
        this.filterChange.emit(filter);
        this.showFilterMenu = false;
    }

    toggleFilterMenu(): void {
        this.showFilterMenu = !this.showFilterMenu;
    }

    getFilterLabel(): string {
        switch (this.activeFilter) {
            case 'active': return 'Active';
            case 'expired': return 'Expired';
            default: return 'All Status';
        }
    }
}
