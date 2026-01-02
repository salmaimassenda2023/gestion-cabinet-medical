import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { PatientFormComponent } from './patient-form/patient-form';
import { Patient } from '../../../core/models/patient.model';
import { PatientService } from '../../../core/services/patient.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';

@Component({
    selector: 'app-secretary-patients',
    standalone: true,
    imports: [CommonModule, ModalComponent, PatientFormComponent],
    templateUrl: './patients.html',
    styleUrls: ['./patients.css']
})
export class SecretaryPatientsComponent implements OnInit {
    patients: Patient[] = [];
    isAddModalOpen = false;
    isEditModalOpen = false;
    isDeleteModalOpen = false;
    selectedPatient?: Patient;
    idCabinet?: number;

    constructor(
        private patientService: PatientService,
        private utilisateurService: UtilisateurService
    ) { }

    ngOnInit() {
        this.loadCurrentUserAndPatients();
    }

    loadCurrentUserAndPatients() {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user) => {
                this.idCabinet = user.idCabinet;
                if (this.idCabinet) {
                    this.loadPatients();
                }
            },
            error: (err) => console.error('Error fetching current user:', err)
        });
    }

    loadPatients() {
        if (this.idCabinet) {
            this.patientService.getPatientsByCabinet(this.idCabinet).subscribe({
                next: (patients) => this.patients = patients,
                error: (err) => console.error('Error fetching patients:', err)
            });
        }
    }

    calculateAge(dateNaissance: string): number {
        if (!dateNaissance) return 0;
        const birthDate = new Date(dateNaissance);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    }

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
        if (this.selectedPatient && this.selectedPatient.id) {
            // Update
            const updatedPatient: Patient = { ...this.selectedPatient, ...patientData };
            this.patientService.updatePatient(this.selectedPatient.id, updatedPatient).subscribe({
                next: () => {
                    this.loadPatients();
                    this.isEditModalOpen = false;
                    this.selectedPatient = undefined;
                },
                error: (err) => console.error('Error updating patient:', err)
            });
        } else {
            // Create
            const newPatient: Patient = {
                ...patientData,
                idCabinet: this.idCabinet
            };
            this.patientService.createPatient(newPatient).subscribe({
                next: () => {
                    this.loadPatients();
                    this.isAddModalOpen = false;
                },
                error: (err) => console.error('Error creating patient:', err)
            });
        }
    }

    confirmDelete() {
        if (this.selectedPatient && this.selectedPatient.id) {
            this.patientService.deletePatient(this.selectedPatient.id).subscribe({
                next: () => {
                    this.loadPatients();
                    this.isDeleteModalOpen = false;
                    this.selectedPatient = undefined;
                },
                error: (err) => console.error('Error deleting patient:', err)
            });
        }
    }
}
