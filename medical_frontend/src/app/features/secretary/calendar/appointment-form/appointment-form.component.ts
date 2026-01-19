import { Component, EventEmitter, Input, OnInit, Output, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PatientService } from '../../../../core/services/patient.service';
import { Patient } from '../../../../core/models/patient.model';
import { StatutRendezVous } from '../../../../core/models/rendezvous.model';
import { Subject } from 'rxjs';
import { takeUntil, finalize } from 'rxjs/operators';

@Component({
    selector: 'app-appointment-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './appointment-form.html',
    styleUrls: ['./appointment-form.css']
})

export class AppointmentFormComponent implements OnInit, OnDestroy, OnChanges {
    @Input() appointment: any;
    @Input() idCabinet?: number;
    @Output() save = new EventEmitter<any>();
    @Output() delete = new EventEmitter<void>();
    @Output() cancel = new EventEmitter<void>();

    appointmentForm: FormGroup;
    patients: Patient[] = [];
    isLoading = false;
    isEditMode = false;
    private destroy$ = new Subject<void>();

    readonly statusOptions = [
        { value: StatutRendezVous.CONFIRME, label: 'Confirmé' },
        { value: StatutRendezVous.ANNULE, label: 'Annulé' }
    ];

    constructor(
        private fb: FormBuilder,
        private patientService: PatientService
    ) {
        this.appointmentForm = this.fb.group({
            patientId: ['', Validators.required],
            date: ['', Validators.required],
            hour: ['', Validators.required],
            type: ['CONSULTATION', Validators.required],
            note: [''],
            statut: ['']
        });
    }

    ngOnInit() {
        console.log('📅 AppointmentForm initialized');
        this.setupForm();
    }

    ngOnChanges(changes: SimpleChanges) {
        console.log('🔄 ngOnChanges triggered:', changes);
        
        if (changes['idCabinet']) {
            console.log('🔄 Cabinet ID changed:', {
                previous: changes['idCabinet'].previousValue,
                current: changes['idCabinet'].currentValue
            });
            
            if (this.idCabinet) {
                console.log('✅ Cabinet ID available, loading patients...');
                this.loadPatients();
            } else {
                console.log('⚠️ Cabinet ID removed, clearing patients');
                this.patients = [];
            }
        }
        
        if (changes['appointment']) {
            this.setupForm();
        }
    }

    setupForm() {
        this.isEditMode = !!this.appointment;
        
        if (this.appointment) {
            console.log('✏️ Editing appointment:', this.appointment);
            const dateObj = this.appointment.dateRdv ? new Date(this.appointment.dateRdv) : new Date();
            const formattedDate = dateObj.toISOString().split('T')[0];
            
            let hour = this.appointment.heureRdv || '09:00';
            if (hour.includes(':')) {
                hour = hour.substring(0, 5);
            }
            
            this.appointmentForm.patchValue({
                patientId: this.appointment.idPatient?.toString(),
                date: formattedDate,
                hour: hour,
                type: this.appointment.motif || 'CONSULTATION',
                note: this.appointment.notes || '',
                statut: this.appointment.statut || StatutRendezVous.CONFIRME
            });

            this.appointmentForm.get('patientId')?.disable();
            
            this.appointmentForm.get('statut')?.setValidators([Validators.required]);
        } else {
            const today = new Date().toISOString().split('T')[0];
            this.appointmentForm.patchValue({
                date: today,
                hour: '09:00',
                type: 'CONSULTATION',
                statut: StatutRendezVous.PLANIFIE
            });
            
            this.appointmentForm.get('patientId')?.enable();
            
            this.appointmentForm.get('statut')?.clearValidators();
        }
        
        this.appointmentForm.get('statut')?.updateValueAndValidity();
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadPatients() {
        if (!this.idCabinet) {
            console.error('❌ Cannot load patients: No cabinet ID');
            this.patients = [];
            return;
        }

        this.isLoading = true;
        console.log('👥 Loading patients for cabinet:', this.idCabinet);
        
        this.patientService.getPatientsByCabinet(this.idCabinet).pipe(
            takeUntil(this.destroy$),
            finalize(() => {
                this.isLoading = false;
                console.log('✅ Patient loading complete');
                console.log('📊 Patients found:', this.patients.length);
            })
        ).subscribe({
            next: (patients) => {
                console.log('✅ Patients loaded successfully:', patients);
                this.patients = patients || [];
                
                if (this.patients.length === 0) {
                    console.log('⚠️ No patients found in cabinet');
                }
            },
            error: (err) => {
                console.error('❌ Error loading patients:', err);
                this.patients = [];
            }
        });
    }

    onSubmit() {
        if (this.appointmentForm.valid || (this.isEditMode && this.appointmentForm.get('patientId')?.disabled)) {
            const formValue = this.appointmentForm.getRawValue(); // getRawValue() includes disabled fields
            
            const submissionData: any = {
                date: formValue.date,
                hour: formValue.hour,
                type: formValue.type,
                note: formValue.note
            };

            if (this.isEditMode) {
                submissionData.statut = formValue.statut;
                console.log('📅 Updating appointment:', submissionData);
            } else {
                submissionData.patientId = Number(formValue.patientId);
                submissionData.statut = StatutRendezVous.PLANIFIE;
                console.log('📅 Creating new appointment:', submissionData);
            }

            this.save.emit(submissionData);
        } else {
            console.log('❌ Form is invalid');
            Object.keys(this.appointmentForm.controls).forEach(key => {
                const control = this.appointmentForm.get(key);
                if (!control?.disabled) {
                    control?.markAsTouched();
                }
            });
        }
    }

    onDelete() {
        console.log('🗑️ Delete requested');
        this.delete.emit();
    }

    onCancel() {
        console.log('❌ Form cancelled');
        this.cancel.emit();
    }
}