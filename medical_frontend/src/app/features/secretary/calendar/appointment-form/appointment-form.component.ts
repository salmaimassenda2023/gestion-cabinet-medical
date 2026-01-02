import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { PatientService } from '../../../../core/services/patient.service';
import { Patient } from '../../../../core/models/patient.model';

@Component({
    selector: 'app-appointment-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './appointment-form.html',
    styleUrls: ['./appointment-form.css']
})
export class AppointmentFormComponent implements OnInit {
    @Input() appointment: any;
    @Input() idCabinet?: number;
    @Output() save = new EventEmitter<any>();
    @Output() delete = new EventEmitter<void>();
    @Output() cancel = new EventEmitter<void>();

    appointmentForm: FormGroup;
    patients: Patient[] = [];

    constructor(
        private fb: FormBuilder,
        private patientService: PatientService
    ) {
        this.appointmentForm = this.fb.group({
            patientId: ['', Validators.required],
            date: ['', Validators.required],
            hour: ['', Validators.required],
            type: ['CONSULTATION', Validators.required],
            note: ['']
        });
    }

    ngOnInit() {
        if (this.idCabinet) {
            this.loadPatients();
        }

        if (this.appointment) {
            this.appointmentForm.patchValue({
                patientId: this.appointment.idPatient,
                date: this.appointment.dateRdv || '',
                hour: this.appointment.heureRdv ? this.appointment.heureRdv.substring(0, 5) : '',
                type: this.appointment.motif,
                note: this.appointment.notes || ''
            });
        }
    }

    loadPatients() {
        if (this.idCabinet) {
            this.patientService.getPatientsByCabinet(this.idCabinet).subscribe({
                next: (patients) => this.patients = patients,
                error: (err) => console.error('Error fetching patients:', err)
            });
        }
    }

    onSubmit() {
        if (this.appointmentForm.valid) {
            this.save.emit(this.appointmentForm.value);
        }
    }

    onDelete() {
        this.delete.emit();
    }

    onCancel() {
        this.cancel.emit();
    }
}
