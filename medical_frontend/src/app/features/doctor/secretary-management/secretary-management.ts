import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { UtilisateurService, UtilisateurResponse, UtilisateurRequest } from '../../auth/services/utilisateur.service';

import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-secretary-management',
    standalone: true,
    imports: [CommonModule, RouterModule, HeaderComponent, ModalComponent, FormsModule],
    templateUrl: './secretary-management.html',
    styleUrls: ['./secretary-management.css']
})
export class SecretaryManagementComponent implements OnInit {
    secretaries: UtilisateurResponse[] = [];
    idCabinet?: number;

    isModalOpen = false;
    isDeleteModalOpen = false;
    editingSecretary: UtilisateurResponse | null = null;
    secretaryToDelete: UtilisateurResponse | null = null;

    // Form Model
    secretaryForm = {
        login: '',
        password: '',
        nom: '',
        prenom: '',
        numTel: '',
        actif: true
    };

    constructor(private utilisateurService: UtilisateurService) { }

    ngOnInit() {
        this.loadCurrentUserAndSecretaries();
    }

    loadCurrentUserAndSecretaries() {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user) => {
                this.idCabinet = user.idCabinet;
                if (this.idCabinet) {
                    this.loadSecretaries();
                }
            },
            error: (err) => console.error('Error fetching current user:', err)
        });
    }

    loadSecretaries() {
        if (this.idCabinet) {
            this.utilisateurService.getUtilisateursByCabinet(this.idCabinet).subscribe({
                next: (users) => {
                    // Filter for only secretaries
                    this.secretaries = users.filter(u => u.role === 'SECRETAIRE');
                },
                error: (err) => console.error('Error fetching secretaries:', err)
            });
        }
    }

    addSecretary() {
        this.editingSecretary = null;
        this.resetForm();
        this.isModalOpen = true;
    }

    editSecretary(sec: UtilisateurResponse) {
        this.editingSecretary = { ...sec };
        this.secretaryForm = {
            login: sec.login,
            password: '', // Password not shown on edit
            nom: sec.nom,
            prenom: sec.prenom,
            numTel: sec.numTel,
            actif: sec.actif
        };
        this.isModalOpen = true;
    }

    confirmDelete(sec: UtilisateurResponse) {
        this.secretaryToDelete = sec;
        this.isDeleteModalOpen = true;
    }

    onConfirmDelete() {
        if (this.secretaryToDelete && this.secretaryToDelete.idUtilisateur) {
            this.utilisateurService.deleteUser(this.secretaryToDelete.idUtilisateur).subscribe({
                next: () => {
                    this.loadSecretaries();
                    this.isDeleteModalOpen = false;
                    this.secretaryToDelete = null;
                },
                error: (err) => console.error('Error deleting secretary:', err)
            });
        }
    }

    saveSecretary() {
        if (!this.secretaryForm.login || !this.secretaryForm.nom || !this.secretaryForm.prenom) return;

        if (this.editingSecretary && this.editingSecretary.idUtilisateur) {
            // Update
            const updateRequest = {
                nom: this.secretaryForm.nom,
                prenom: this.secretaryForm.prenom,
                numTel: this.secretaryForm.numTel
            };
            this.utilisateurService.updateUtilisateur(this.editingSecretary.idUtilisateur, updateRequest).subscribe({
                next: () => {
                    // Handle status separately if it changed
                    if (this.editingSecretary && this.editingSecretary.actif !== this.secretaryForm.actif) {
                        this.updateStatus(this.editingSecretary.idUtilisateur!, this.secretaryForm.actif);
                    } else {
                        this.loadSecretaries();
                        this.isModalOpen = false;
                        this.resetForm();
                    }
                },
                error: (err) => console.error('Error updating secretary:', err)
            });
        } else {
            // Create
            const newSecretary: UtilisateurRequest = {
                login: this.secretaryForm.login,
                password: this.secretaryForm.password,
                nom: this.secretaryForm.nom,
                prenom: this.secretaryForm.prenom,
                numTel: this.secretaryForm.numTel,
                role: 'SECRETAIRE',
                idCabinet: this.idCabinet
            };
            this.utilisateurService.createUtilisateur(newSecretary).subscribe({
                next: () => {
                    this.loadSecretaries();
                    this.isModalOpen = false;
                    this.resetForm();
                },
                error: (err) => console.error('Error creating secretary:', err)
            });
        }
    }

    private updateStatus(id: number, active: boolean) {
        this.utilisateurService.updateUserStatus(id, active).subscribe({
            next: () => {
                this.loadSecretaries();
                this.isModalOpen = false;
                this.resetForm();
            },
            error: (err) => console.error('Error updating secretary status:', err)
        });
    }

    private resetForm() {
        this.secretaryForm = {
            login: '',
            password: '',
            nom: '',
            prenom: '',
            numTel: '',
            actif: true
        };
    }
}
