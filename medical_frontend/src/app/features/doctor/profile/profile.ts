import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'app-doctor-profile',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './profile.html',
    styleUrls: ['./profile.css']
})
export class DoctorProfileComponent implements OnInit {
    personalForm: FormGroup;
    passwordForm: FormGroup;

    constructor(private fb: FormBuilder) {
        this.personalForm = this.fb.group({
            firstName: ['Farah', Validators.required],
            lastName: ['Doctor', Validators.required],
            email: ['doctor.farah@example.com', [Validators.required, Validators.email]],
            phone: ['+212 600-000000', Validators.required],
            specialty: ['Cardiologist', Validators.required],
            clinicName: ['Heart Center', Validators.required],
            clinicAddress: ['123 Medical Center, Casablanca', Validators.required]
        });

        this.passwordForm = this.fb.group({
            oldPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required]
        }, { validator: this.passwordMatchValidator });
    }

    ngOnInit(): void { }

    passwordMatchValidator(g: FormGroup) {
        return g.get('newPassword')?.value === g.get('confirmPassword')?.value
            ? null : { mismatch: true };
    }

    onUpdatePersonal(): void {
        if (this.personalForm.valid) {
            console.log('Updating Doctor Info:', this.personalForm.value);
            alert('Profile updated successfully!');
        }
    }

    onUpdatePassword(): void {
        if (this.passwordForm.valid) {
            console.log('Updating Password:', this.passwordForm.value);
            alert('Password updated successfully!');
            this.passwordForm.reset();
        }
    }
}
