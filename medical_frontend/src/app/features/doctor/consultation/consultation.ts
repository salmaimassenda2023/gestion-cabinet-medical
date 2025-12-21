import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormArray } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
    selector: 'app-consultation',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './consultation.html',
    styleUrls: ['./consultation.css']
})
export class ConsultationComponent implements OnInit {
    consultationForm: FormGroup;

    patientInfo = {
        name: 'Alan Kevein',
        gender: 'Male',
        age: 32,
        bloodType: 'O+',
        allergy: 'Penicillin',
        currentTreatment: 'None',
        lifestyle: 'No smoking . Balanced Diet'
    };

    medicalHistory = [
        { date: '12/12/2024', type: 'Consultation', diagnostic: 'Check-up' },
        { date: '18/12/2024', type: 'Follow-up', diagnostic: 'Stable' }
    ];

    constructor(private fb: FormBuilder) {
        this.consultationForm = this.fb.group({
            temperature: [''],
            bloodPressure: [''],
            heartRate: [''],
            observation: [''],
            additionalExams: this.fb.group({
                bloodTests: [false],
                urineTests: [false],
                ecg: [false],
                xray: [false],
                mri: [false],
                ctScan: [false],
                ultrasound: [false]
            }),
            prescription: this.fb.array([]),
            diagnostic: [''],
            followUpType: ['Months'],
            followUpValue: ['']
        });
    }

    ngOnInit() { }

    onDone() {
        console.log('Consultation Form Submitted:', this.consultationForm.value);
    }
}
