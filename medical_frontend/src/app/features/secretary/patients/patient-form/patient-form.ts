import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
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
    @Input() idCabinet?: number;
    @Output() save = new EventEmitter<any>();
    @Output() cancel = new EventEmitter<void>();

    patientForm: FormGroup;

    constructor(private fb: FormBuilder) {
        this.patientForm = this.fb.group({
            cin: ['', Validators.required],
            nom: ['', Validators.required],
            prenom: ['', Validators.required],
            dateNaissance: ['', Validators.required],
            telephone: ['', [Validators.required,
                Validators.pattern(/^\+?[0-9]{10,20}$/)]],
            email: ['', [Validators.email]],
            adresse: [''],
            typeMutuelle: [''],
            numeroMutuelle: [''],
            sexe: ['F', Validators.required],
            idCabinet: [null, Validators.required]
        });
    }

    ngOnInit() {
        console.log('🔨 PatientFormComponent initialized');
        console.log('📋 idCabinet from parent:', this.idCabinet);
        console.log('📋 Patient data (if editing):', this.patient);
        
        // Set the cabinet ID from the parent input
        if (this.idCabinet) {
            console.log('✅ Setting idCabinet in form:', this.idCabinet);
            this.patientForm.patchValue({ idCabinet: this.idCabinet });
        } else {
            console.error('❌ No idCabinet provided from parent!');
        }
        
        if (this.patient) {
            console.log('📝 Patching patient data into form');
            this.patientForm.patchValue(this.patient);
            
            if (this.idCabinet && !this.patient.idCabinet) {
                console.log('🔄 Ensuring idCabinet is set:', this.idCabinet);
                this.patientForm.patchValue({ idCabinet: this.idCabinet });
            }
        }
        
        console.log('📊 Final form values:', this.patientForm.value);
    }

    onSubmit() {
        console.log('💾 Form submitted');
        console.log('📋 Form values:', this.patientForm.value);
        console.log('✅ Form valid?', this.patientForm.valid);
        
        if (this.patientForm.valid) {
            const formValue = this.patientForm.value;
            console.log('📤 Emitting save event with idCabinet:', formValue.idCabinet);
            this.save.emit(formValue);
        } else {
            console.log('❌ Form is invalid');
            Object.keys(this.patientForm.controls).forEach(key => {
                const control = this.patientForm.get(key);
                if (control?.invalid) {
                    console.log(`❌ Invalid field ${key}:`, control.errors);
                }
                control?.markAsTouched();
            });
        }
    }

    onCancel() {
        console.log('❌ Form cancelled');
        this.cancel.emit();
    }
}