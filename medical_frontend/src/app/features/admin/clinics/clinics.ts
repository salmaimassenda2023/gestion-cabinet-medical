import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { ClinicTableComponent, Clinic } from '../../../shared/components/clinic-table/clinic-table.component';
import { ClinicFormComponent } from '../../../shared/components/clinic-form/clinic-form.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

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
    clinics: Clinic[] = [
        {
            id: 'CL-001',
            logo: 'assets/clinic-1.png',
            name: 'St. Mary Medical Center',
            address: '123 Health Ave, NY',
            phone: '+1 234 567 890',
            specialty: 'Cardiology',
            doctor: 'Dr. John Doe',
            status: 'active'
        },
        {
            id: 'CL-002',
            logo: 'assets/clinic-2.png',
            name: 'Oakcrest Family Clinic',
            address: '456 Wellness Rd, CA',
            phone: '+1 987 654 321',
            specialty: 'Pediatrics',
            doctor: 'Dr. Sarah Connor',
            status: 'active'
        },
        {
            id: 'CL-003',
            logo: 'assets/clinic-3.png',
            name: 'Peak Vision Center',
            address: '789 Sight Blvd, TX',
            phone: '+1 555 012 345',
            specialty: 'Ophthalmology',
            doctor: 'Dr. Michael Smith',
            status: 'deactivate'
        }
    ];

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

    ngOnInit(): void { }

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
        const newClinic: Clinic = {
            ...newClinicData as Clinic,
            id: `CL-00${this.clinics.length + 1}`
        };
        this.clinics = [...this.clinics, newClinic];
        this.isAddModalOpen = false;
    }

    editClinic(clinic: Clinic): void {
        this.selectedClinic = { ...clinic };
        this.isEditModalOpen = true;
    }

    onUpdateClinic(updatedData: Partial<Clinic>): void {
        if (this.selectedClinic) {
            this.clinics = this.clinics.map(c =>
                c.id === this.selectedClinic!.id ? { ...c, ...updatedData } : c
            );
            this.isEditModalOpen = false;
            this.selectedClinic = undefined;
        }
    }

    deleteClinic(clinic: Clinic): void {
        this.selectedClinic = clinic;
        this.isDeleteModalOpen = true;
    }

    confirmDelete(): void {
        if (this.selectedClinic) {
            this.clinics = this.clinics.filter(c => c.id !== this.selectedClinic!.id);
            this.isDeleteModalOpen = false;
            this.selectedClinic = undefined;
        }
    }

    logout(): void {
        this.isLogoutModalOpen = true;
    }

    confirmLogout(): void {
        console.log('Logging out...');
        // Implement actual logout logic here
        this.isLogoutModalOpen = false;
    }
}
