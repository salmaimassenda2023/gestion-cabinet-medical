import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { MedicamentTableComponent, Medicament } from '../../../shared/components/medicament-table/medicament-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
    selector: 'app-medicaments',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        HeaderComponent,
        SidebarComponent,
        MedicamentTableComponent,
        ModalComponent
    ],
    templateUrl: './medicaments.html',
    styleUrls: ['./medicaments.css']
})
export class MedicamentsComponent implements OnInit {
    medicaments: Medicament[] = [
        { id: 'MED-001', name: 'Paracetamol 500mg' },
        { id: 'MED-002', name: 'Amoxicillin 250mg' },
        { id: 'MED-003', name: 'Ibuprofen 400mg' }
    ];

    searchTerm: string = '';
    isLogoutModalOpen = false;
    isDeleteModalOpen = false;
    selectedMed?: Medicament;

    @ViewChild('fileInput') fileInput?: ElementRef<HTMLInputElement>;

    ngOnInit(): void { }

    get filteredMedicaments(): Medicament[] {
        if (!this.searchTerm) return this.medicaments;
        const term = this.searchTerm.toLowerCase();
        return this.medicaments.filter(m =>
            m.name.toLowerCase().includes(term) ||
            m.id.toLowerCase().includes(term)
        );
    }

    onSearch(term: string): void {
        this.searchTerm = term;
    }

    triggerImport(): void {
        this.fileInput?.nativeElement.click();
    }

    onFileImported(event: any): void {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = (e: any) => {
                const text = e.target.result;
                this.parseMedicaments(text);
            };
            reader.readAsText(file);
        }
    }

    private parseMedicaments(text: string): void {
        // Simple parsing: each line is a medicament name
        const lines = text.split(/\r?\n/).filter((line: string) => line.trim().length > 0);
        const newMeds: Medicament[] = lines.map((name: string, index: number) => ({
            id: `MED-IMP-${this.medicaments.length + index + 1}`,
            name: name.trim()
        }));
        this.medicaments = [...this.medicaments, ...newMeds];
    }

    editMed(med: Medicament): void {
        // For now, names are just text, so edit could be restricted or simple prompt
        const newName = prompt('Edit Medicament Name:', med.name);
        if (newName && newName.trim() !== '') {
            this.medicaments = this.medicaments.map(m =>
                m.id === med.id ? { ...m, name: newName.trim() } : m
            );
        }
    }

    deleteMed(med: Medicament): void {
        this.selectedMed = med;
        this.isDeleteModalOpen = true;
    }

    confirmDelete(): void {
        if (this.selectedMed) {
            this.medicaments = this.medicaments.filter(m => m.id !== this.selectedMed!.id);
            this.isDeleteModalOpen = false;
            this.selectedMed = undefined;
        }
    }

    logout(): void {
        this.isLogoutModalOpen = true;
    }

    confirmLogout(): void {
        console.log('Logging out...');
        this.isLogoutModalOpen = false;
    }
}
