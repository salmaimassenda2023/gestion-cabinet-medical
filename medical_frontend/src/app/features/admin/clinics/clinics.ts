import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { ClinicTableComponent, Clinic } from '../../../shared/components/clinic-table/clinic-table.component';
import { ClinicFormComponent } from '../../../shared/components/clinic-form/clinic-form.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { CabinetService, CabinetResponse } from '../../doctor/services/cabinet.service';

@Component({
    selector: 'app-clinics',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        HeaderComponent,
        SidebarComponent,
        ClinicTableComponent,
        ClinicFormComponent,
        ModalComponent
    ],
    templateUrl: './clinics.html',
    styleUrls: ['./clinics.css']
})
export class ClinicsComponent implements OnInit {
    clinics: Clinic[] = [];
    allClinics: Clinic[] = [];

    doctors = [
        { id: '1', name: 'Dr. John Doe' }, 
        { id: '2', name: 'Dr. Sarah Connor' },
        { id: '3', name: 'Dr. Michael Smith' },
        { id: '4', name: 'Dr. Emma Watson' }
    ];

    activeFilter: 'all' | 'active' | 'deactivate' = 'all';
    searchTerm: string = '';
    isLogoutModalOpen = false;
    isAddModalOpen = false;
    isEditModalOpen = false;
    isDeleteModalOpen = false;
    selectedClinic?: Clinic;

    constructor(private cabinetService: CabinetService) { }

    ngOnInit(): void {
        this.loadClinics();
    }

loadClinics() {
    this.cabinetService.getAllCabinets().subscribe({
        next: (cabinets) => {
            console.log('Loaded cabinets:', cabinets); 
            
            this.allClinics = cabinets.map(c => ({
                id: c.id.toString(),
                logo: c.logo || 'assets/clinic-1.png',
                name: c.nom,
                address: c.adresse,
                phone: c.numTel || '', 
                specialty: c.specialite || 'General',
                doctor:(c.medecinPrenom && c.medecinNom ? `${c.medecinPrenom} ${c.medecinNom}` : 
                       (c.medecinId ? `Doctor ID: ${c.medecinId}` : 'Not Assigned')),
                status: 'active' 
            }));
            
            this.clinics = [...this.allClinics];
            console.log('Mapped clinics:', this.clinics); 
        },
        error: (err) => {
            console.error('Error loading clinics:', err);
        }
    });
}

    get filteredClinics(): Clinic[] {
        let filtered = this.clinics;

        // Status filter
        if (this.activeFilter !== 'all') {
            filtered = filtered.filter(c => c.status === this.activeFilter);
        }

        // Search filter
        if (this.searchTerm) {
            const term = this.searchTerm.toLowerCase();
            filtered = filtered.filter(c =>
                c.name.toLowerCase().includes(term) ||
                c.address.toLowerCase().includes(term) ||
                c.phone.toLowerCase().includes(term) ||
                c.specialty.toLowerCase().includes(term) ||
                c.doctor.toLowerCase().includes(term) ||
                c.id.toLowerCase().includes(term)
            );
        }

        return filtered;
    }

    onSearch(term: string): void {
        this.searchTerm = term;
    }

    onFilterChange(filter: 'all' | 'active' | 'deactivate'): void {
        this.activeFilter = filter;
    }

    addClinic(): void {
        this.isAddModalOpen = true;
    }

    onSaveNewClinic(newClinicData: Partial<Clinic>): void {
        const cabinetData = {
            nom: newClinicData.name,
            adresse: newClinicData.address,
            numTel: newClinicData.phone,
            emailContact: 'contact@example.com', 
            tarifConsultation: 300, 
            specialite: newClinicData.specialty || 'General'
        };

        this.cabinetService.createCabinet(cabinetData).subscribe({
            next: () => {
                this.loadClinics();
                this.isAddModalOpen = false;
            },
            error: (err) => console.error('Error creating clinic:', err)
        });
    }

    editClinic(clinic: Clinic): void {
        this.selectedClinic = { ...clinic };
        this.isEditModalOpen = true;
    }

    onUpdateClinic(updatedData: Partial<Clinic>): void {
        if (this.selectedClinic) {
            const cabinetData = {
                nom: updatedData.name,
                adresse: updatedData.address,
                numTel: updatedData.phone,
                specialite: updatedData.specialty
            };

            this.cabinetService.updateCabinet(parseInt(this.selectedClinic.id), cabinetData).subscribe({
                next: () => {
                    this.loadClinics();
                    this.isEditModalOpen = false;
                    this.selectedClinic = undefined;
                },
                error: (err) => console.error('Error updating clinic:', err)
            });
        }
    }

    deleteClinic(clinic: Clinic): void {
        this.selectedClinic = clinic;
        this.isDeleteModalOpen = true;
    }

    confirmDelete(): void {
        if (this.selectedClinic) {
            this.cabinetService.deleteCabinet(parseInt(this.selectedClinic.id)).subscribe({
                next: () => {
                    this.loadClinics();
                    this.isDeleteModalOpen = false;
                    this.selectedClinic = undefined;
                },
                error: (err) => console.error('Error deleting clinic:', err)
            });
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
