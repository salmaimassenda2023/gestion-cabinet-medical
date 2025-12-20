import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

export interface Medicament {
    id: string;
    name: string;
}

@Component({
    selector: 'app-medicament-table',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './medicament-table.component.html',
    styleUrls: ['./medicament-table.component.css']
})
export class MedicamentTableComponent {
    @Input() medicaments: Medicament[] = [];
    @Input() currentPage: number = 1;
    @Input() totalPages: number = 1;

    @Output() import = new EventEmitter<void>();
    @Output() edit = new EventEmitter<Medicament>();
    @Output() delete = new EventEmitter<Medicament>();
    @Output() pageChange = new EventEmitter<number>();

    onImport(): void {
        this.import.emit();
    }

    onEdit(med: Medicament): void {
        this.edit.emit(med);
    }

    onDelete(med: Medicament): void {
        this.delete.emit(med);
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
}
