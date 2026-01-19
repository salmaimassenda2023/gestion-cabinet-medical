import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core'; // Ajoutez ChangeDetectorRef
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { PatientFormComponent } from './patient-form/patient-form';
import { Patient } from '../../../core/models/patient.model';
import { PatientService } from '../../../core/services/patient.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { CabinetService } from '../../doctor/services/cabinet.service';
import { switchMap, catchError, tap, finalize, takeUntil } from 'rxjs/operators'; // Ajoutez takeUntil
import { of, Subject } from 'rxjs';

@Component({
    selector: 'app-secretary-patients',
    standalone: true,
    imports: [CommonModule, ModalComponent, PatientFormComponent],
    templateUrl: './patients.html',
    styleUrls: ['./patients.css']
})
export class SecretaryPatientsComponent implements OnInit, OnDestroy { // Ajoutez OnDestroy
    patients: Patient[] = [];
    isAddModalOpen = false;
    isEditModalOpen = false;
    isDeleteModalOpen = false;
    selectedPatient?: Patient;
    idCabinet!: number;
    isLoading = true;
    errorMessage = '';
    dataLoaded = false;

    private destroy$ = new Subject<void>();

    constructor(
        private patientService: PatientService,
        private utilisateurService: UtilisateurService,
        private cabinetService: CabinetService,
        private cdr: ChangeDetectorRef 
    ) { 
        console.log('🔨 SecretaryPatientsComponent constructor called');
    }

    ngOnInit() {
        console.log('🔨 SecretaryPatientsComponent initialized');
        this.loadData(); 
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
    }

    trackById(index: number, item: Patient): number {
        return item.id || index;
    }
loadData() {
    console.log('🚀 loadData() called');
    this.isLoading = true;
    this.errorMessage = '';
    
    // First get current user
    this.utilisateurService.getCurrentUser().pipe(
        takeUntil(this.destroy$),
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
                throw new Error('No cabinet found');
            }
            
            this.idCabinet = cabinet.id;
            console.log('✅ Cabinet ID set:', this.idCabinet);
        }),
        switchMap(() => {
            return this.patientService.getPatientsByCabinet(this.idCabinet);
        }),
        catchError(err => {
            console.error('❌ Error:', err);
            this.errorMessage = err.message || 'Error loading data';
            return of([]);
        }),
        finalize(() => {
            this.isLoading = false;
            this.cdr.detectChanges();
            console.log('🔄 Change detection triggered');
        })
    ).subscribe({
        next: (patients) => {
            console.log('✅ Patients received:', patients);
            this.patients = patients || [];
            console.log(`✅ ${this.patients.length} patients loaded successfully`);
            this.dataLoaded = true;
        },
        error: (err) => {
            console.error('❌ Subscription error:', err);
            this.errorMessage = 'Failed to load data';
        }
    });
}
    openAddModal() {
        console.log('➕ Opening Add Modal');
        
        if (!this.idCabinet) {
            console.error('❌ Cannot open add modal: No cabinet ID');
            this.errorMessage = 'Cabinet information not available. Please refresh.';
            return;
        }
        
        this.selectedPatient = undefined;
        this.isAddModalOpen = true;
        console.log('✅ Add modal opened');
    }

    openEditModal(patient: Patient) {
        console.log('✏️ Opening Edit Modal for patient:', patient);
        
        if (!this.idCabinet) {
            console.error('❌ Cannot open edit modal: No cabinet ID');
            this.errorMessage = 'Cabinet information not available. Please refresh.';
            return;
        }
        
        this.selectedPatient = patient;
        this.isEditModalOpen = true;
        console.log('✅ Edit modal opened');
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

    openDeleteModal(patient: Patient) {
        this.selectedPatient = patient;
        this.isDeleteModalOpen = true;
    }

    onSavePatient(patientData: any) {
        console.log('💾 Saving patient data:', patientData);
        
        if (!patientData.idCabinet && this.idCabinet) {
            patientData.idCabinet = this.idCabinet;
        }
        
        this.isLoading = true;
        
        if (this.selectedPatient && this.selectedPatient.id) {
            const updatedPatient: Patient = { ...this.selectedPatient, ...patientData };
            console.log('🔄 Updating patient:', updatedPatient);
            
            this.patientService.updatePatient(this.selectedPatient.id, updatedPatient).pipe(
                finalize(() => {
                    this.isLoading = false;
                    this.cdr.detectChanges();
                })
            ).subscribe({
                next: () => {
                    this.loadData();
                    this.isEditModalOpen = false;
                    this.selectedPatient = undefined;
                },
                error: (err) => {
                    console.error('Error updating patient:', err);
                    this.errorMessage = 'Error updating patient.';
                }
            });
        } else {
            const newPatient: Patient = {
                ...patientData,
                idCabinet: this.idCabinet
            };
            console.log('➕ Creating patient with idCabinet:', newPatient.idCabinet);
            
            this.patientService.createPatient(newPatient).pipe(
                finalize(() => {
                    this.isLoading = false;
                    this.cdr.detectChanges();
                })
            ).subscribe({
                next: () => {
                    this.loadData();
                    this.isAddModalOpen = false;
                },
                error: (err) => {
                    console.error('Error creating patient:', err);
                    this.errorMessage = 'Error creating patient.';
                }
            });
        }
    }

    onCancel() {
        this.isAddModalOpen = false;
        this.isEditModalOpen = false;
        this.selectedPatient = undefined;
    }

    confirmDelete() {
        if (this.selectedPatient && this.selectedPatient.id) {
            this.isLoading = true;
            
            this.patientService.deletePatient(this.selectedPatient.id).pipe(
                finalize(() => {
                    this.isLoading = false;
                    this.cdr.detectChanges();
                })
            ).subscribe({
                next: () => {
                    this.loadData();
                    this.isDeleteModalOpen = false;
                    this.selectedPatient = undefined;
                },
                error: (err) => {
                    console.error('Error deleting patient:', err);
                    this.errorMessage = 'Error deleting patient.';
                }
            });
        }
    }

    refreshData() {
        this.loadData();
    }
}