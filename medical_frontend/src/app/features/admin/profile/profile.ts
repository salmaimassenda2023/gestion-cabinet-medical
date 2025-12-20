import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

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

    constructor(private fb: FormBuilder) {
        this.personalForm = this.fb.group({
            firstName: ['Farah', Validators.required],
            lastName: ['Admin', Validators.required],
            email: ['farah@clinicflow.com', [Validators.required, Validators.email]],
            phone: ['+212 600-000000', Validators.required]
        });

        this.passwordForm = this.fb.group({
            oldPassword: ['', Validators.required],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
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
            console.log('Updating Personal Info:', this.personalForm.value);
            alert('Personal information updated successfully!');
        }
    }

    onUpdatePassword(): void {
        if (this.passwordForm.valid) {
            console.log('Updating Password:', this.passwordForm.value);
            alert('Password updated successfully!');
            this.passwordForm.reset();
        }
    }

    logout(): void {
        this.isLogoutModalOpen = true;
    }

    confirmLogout(): void {
        console.log('Logging out...');
        this.isLogoutModalOpen = false;
    }
}
