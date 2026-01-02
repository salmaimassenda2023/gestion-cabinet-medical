import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { UtilisateurService, UtilisateurResponse } from '../../auth/services/utilisateur.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

@Component({
    selector: 'app-profile',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        HeaderComponent,
        SidebarComponent,
        ModalComponent
    ],
    templateUrl: './profile.html',
    styleUrls: ['./profile.css']
})
export class ProfileComponent implements OnInit {
    personalForm: FormGroup;
    passwordForm: FormGroup;
    isLogoutModalOpen = false;
    currentUser?: UtilisateurResponse;

    constructor(
        private fb: FormBuilder,
        private utilisateurService: UtilisateurService,
        private router: Router
    ) {
        this.personalForm = this.fb.group({
            firstName: ['', Validators.required],
            lastName: ['', Validators.required],
            email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
            phone: ['', Validators.required]
        });

        this.passwordForm = this.fb.group({
            oldPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
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
                    alert('Personal information updated successfully!');
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

    logout(): void {
        this.isLogoutModalOpen = true;
    }

    confirmLogout(): void {
        this.isLogoutModalOpen = false;
        localStorage.clear();
        sessionStorage.clear();
        this.router.navigate(['/login']);
    }
}
