import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Clinic {
    id: string;
    logo: string;
    name: string;
    address: string;
    phone: string;
    specialty: string;
    doctor: string; 
    status: 'active' | 'deactivate';
}

@Component({
    selector: 'app-clinic-table',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './clinic-table.component.html',
    styleUrls: ['./clinic-table.component.css']
})
export class ClinicTableComponent {
    @Input() clinics: Clinic[] = [];
    @Input() currentPage: number = 1;
    @Input() totalPages: number = 1;
    @Input() activeFilter: 'all' | 'active' | 'deactivate' = 'all';

    @Output() add = new EventEmitter<void>();
    @Output() edit = new EventEmitter<Clinic>();
    @Output() delete = new EventEmitter<Clinic>();
    @Output() pageChange = new EventEmitter<number>();
    @Output() filterChange = new EventEmitter<'all' | 'active' | 'deactivate'>();

    showFilterMenu = false;

    getStatusClass(status: 'active' | 'deactivate'): string {
        return status === 'active' ? 'status-active' : 'status-deactivate';
    }

    getStatusText(status: 'active' | 'deactivate'): string {
        return status === 'active' ? 'Active' : 'Deactivate';
    }

    onAdd(): void {
        this.add.emit();
    }

    onEdit(clinic: Clinic): void {
        this.edit.emit(clinic);
    }

    onDelete(clinic: Clinic): void {
        this.delete.emit(clinic);
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

    setFilter(filter: 'all' | 'active' | 'deactivate'): void {
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
            case 'deactivate': return 'Deactivate';
            default: return 'All Status';
        }
    }
}
