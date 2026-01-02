import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Patient } from '../../../../core/models/patient.model';

@Component({
    selector: 'app-patient-form',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './patient-form.html',
    styleUrls: ['./patient-form.css']
})
export class PatientFormComponent implements OnInit {
    @Input() patient?: Patient;
    @Output() save = new EventEmitter<any>();
    @Output() cancel = new EventEmitter<void>();

    patientForm: FormGroup;

    constructor(private fb: FormBuilder) {
        this.patientForm = this.fb.group({
            cin: ['', Validators.required],
            nom: ['', Validators.required],
            prenom: ['', Validators.required],
            dateNaissance: ['', Validators.required],
            telephone: ['', [Validators.pattern(/^\+?[0-9]{10,20}$/)]],
            email: ['', [Validators.email]],
            adresse: [''],
            typeMutuelle: [''],
            numeroMutuelle: [''],
            sexe: ['F', Validators.required]
        });
    }

    ngOnInit() {
        if (this.patient) {
            this.patientForm.patchValue(this.patient);
        }
    }

    onSubmit() {
        if (this.patientForm.valid) {
            this.save.emit(this.patientForm.value);
        }
    }

    onCancel() {
        this.cancel.emit();
    }
}
