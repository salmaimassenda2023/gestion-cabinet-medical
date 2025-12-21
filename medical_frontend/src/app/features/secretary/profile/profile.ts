import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';

@Component({
    selector: 'app-secretary-profile',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './profile.html',
    styleUrls: ['./profile.css']
})
export class SecretaryProfileComponent implements OnInit {
    personalForm: FormGroup;
    passwordForm: FormGroup;

    constructor(private fb: FormBuilder) {
        this.personalForm = this.fb.group({
            firstName: ['Jane', Validators.required],
            lastName: ['Secretary', Validators.required],
            email: ['secretary@clinic.com', [Validators.required, Validators.email]],
            phone: ['+212 600-000000', Validators.required],
            address: ['Clinic Main Ave, Casablanca', Validators.required]
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
            console.log('Updating Secretary Info:', this.personalForm.value);
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
