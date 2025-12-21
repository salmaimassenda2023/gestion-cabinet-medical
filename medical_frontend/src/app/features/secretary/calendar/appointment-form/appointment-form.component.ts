import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
    selector: 'app-appointment-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './appointment-form.html',
    styleUrls: ['./appointment-form.css']
})
export class AppointmentFormComponent implements OnInit {
    @Input() appointment: any;
    @Output() save = new EventEmitter<any>();
    @Output() delete = new EventEmitter<void>();
    @Output() cancel = new EventEmitter<void>();

    appointmentForm: FormGroup;

    constructor(private fb: FormBuilder) {
        this.appointmentForm = this.fb.group({
            patientName: ['', Validators.required],
            date: ['', Validators.required],
            hour: ['', Validators.required],
            type: ['Consultation', Validators.required],
            note: ['']
        });
    }

    ngOnInit() {
        if (this.appointment) {
            this.appointmentForm.patchValue({
                patientName: this.appointment.patientName,
                date: this.appointment.date || '',
                hour: this.appointment.time,
                type: this.appointment.type,
                note: this.appointment.note || ''
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
