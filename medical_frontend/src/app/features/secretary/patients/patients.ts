import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { PatientFormComponent } from './patient-form/patient-form';
import { Patient } from '../../../core/models/patient.model';

@Component({
    selector: 'app-secretary-patients',
    standalone: true,
    imports: [CommonModule, ModalComponent, PatientFormComponent],
    templateUrl: './patients.html',
    styleUrls: ['./patients.css']
})
export class SecretaryPatientsComponent implements OnInit {
    patients: Patient[] = [
        { id: '1', cin: 'AB123456', name: 'Peter Mullin', age: 45, mutuelleType: 'CNAM', gender: 'Male' },
        { id: '2', cin: 'CD789012', name: 'Sandra Bay', age: 32, mutuelleType: 'CIMR', gender: 'Female' },
        { id: '3', cin: 'EF345678', name: 'Andrew Kim', age: 28, mutuelleType: 'Private', gender: 'Male' }
    ];

    isAddModalOpen = false;
    isEditModalOpen = false;
    isDeleteModalOpen = false;
    selectedPatient?: Patient;

    ngOnInit() { }

    openAddModal() {
        this.selectedPatient = undefined;
        this.isAddModalOpen = true;
    }

    openEditModal(patient: Patient) {
        this.selectedPatient = patient;
        this.isEditModalOpen = true;
    }

    openDeleteModal(patient: Patient) {
        this.selectedPatient = patient;
        this.isDeleteModalOpen = true;
    }

    onSavePatient(patientData: any) {
        if (this.selectedPatient) {
            const index = this.patients.findIndex(p => p.id === this.selectedPatient?.id);
            this.patients[index] = { ...this.selectedPatient, ...patientData };
            this.isEditModalOpen = false;
        } else {
            const newPatient: Patient = {
                id: (this.patients.length + 1).toString(),
                ...patientData
            };
            this.patients = [...this.patients, newPatient];
            this.isAddModalOpen = false;
        }
        this.selectedPatient = undefined;
    }

    confirmDelete() {
        if (this.selectedPatient) {
            this.patients = this.patients.filter(p => p.id !== this.selectedPatient?.id);
            this.isDeleteModalOpen = false;
            this.selectedPatient = undefined;
        }
    }
}
