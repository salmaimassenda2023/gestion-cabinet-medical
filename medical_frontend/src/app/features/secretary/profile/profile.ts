import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UtilisateurService, UtilisateurResponse } from '../../auth/services/utilisateur.service';
import { HttpErrorResponse } from '@angular/common/http';

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
    currentUser?: UtilisateurResponse;

    constructor(
        private fb: FormBuilder,
        private utilisateurService: UtilisateurService
    ) {
        this.personalForm = this.fb.group({
            firstName: ['', Validators.required],
            lastName: ['', Validators.required],
            email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
            phone: ['', Validators.required],
            address: [{ value: 'N/A', disabled: true }] // Not stored in utilisateur-service yet
        });

        this.passwordForm = this.fb.group({
            oldPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required]
        }, { validator: this.passwordMatchValidator });
    }

    ngOnInit(): void {
        this.loadProfile();
    }

    loadProfile(): void {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user: UtilisateurResponse) => {
                this.currentUser = user;
                this.personalForm.patchValue({
                    firstName: user.prenom,
                    lastName: user.nom,
                    email: user.login,
                    phone: user.numTel
                });
            },
            error: (err: HttpErrorResponse) => console.error('Error loading profile:', err)
        });
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('newPassword')?.value === g.get('confirmPassword')?.value
            ? null : { mismatch: true };
    }

    onUpdatePersonal(): void {
        if (this.personalForm.valid && this.currentUser) {
            const request = {
                nom: this.personalForm.value.lastName,
                prenom: this.personalForm.value.firstName,
                numTel: this.personalForm.value.phone
            };

            this.utilisateurService.updateUtilisateur(this.currentUser.idUtilisateur, request).subscribe({
                next: () => {
                    alert('Profile updated successfully!');
                },
                error: (err: HttpErrorResponse) => alert('Failed to update profile: ' + err.message)
            });
        }
    }

    onUpdatePassword(): void {
        if (this.passwordForm.valid && this.currentUser) {
            const request = {
                oldPassword: this.passwordForm.value.oldPassword,
                newPassword: this.passwordForm.value.newPassword
            };

            this.utilisateurService.changePassword(this.currentUser.idUtilisateur, request).subscribe({
                next: () => {
                    alert('Password updated successfully!');
                    this.passwordForm.reset();
                },
                error: (err: HttpErrorResponse) => alert('Failed to update password: ' + err.message)
            });
        }
    }
}
