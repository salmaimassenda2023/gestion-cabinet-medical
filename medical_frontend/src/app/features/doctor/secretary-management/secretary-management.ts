import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-secretary-management',
    standalone: true,
    imports: [CommonModule, RouterModule, HeaderComponent, ModalComponent, FormsModule],
    templateUrl: './secretary-management.html',
    styleUrls: ['./secretary-management.css']
})
export class SecretaryManagementComponent implements OnInit {
    secretaries = [
        { id: 1, name: 'Alice Smith', email: 'alice@example.com', phone: '123-456-7890', address: '123 Main St, New York', status: 'active' },
        { id: 2, name: 'Bob Jones', email: 'bob@example.com', phone: '098-765-4321', address: '456 Oak Ave, Los Angeles', status: 'active' },
        { id: 3, name: 'Claire Brown', email: 'claire@example.com', phone: '555-0199', address: '789 Pine Rd, Chicago', status: 'deactivate' }
    ];

    isModalOpen = false;
    isDeleteModalOpen = false;
    editingSecretary: any = null;
    secretaryToDelete: any = null;

    // Form Model
    secretaryForm = {
        id: 0,
        name: '',
        email: '',
        phone: '',
        address: '',
        status: 'active'
    };

    ngOnInit() { }

    onLogout() {
        console.log('Logging out...');
    }

    onSearch(term: string) {
        console.log('Searching for:', term);
    }

    addSecretary() {
        this.editingSecretary = null;
        this.resetForm();
        this.isModalOpen = true;
    }

    editSecretary(sec: any) {
        this.editingSecretary = { ...sec };
        this.secretaryForm = { ...sec };
        this.isModalOpen = true;
    }

    confirmDelete(sec: any) {
        this.secretaryToDelete = sec;
        this.isDeleteModalOpen = true;
    }

    onConfirmDelete() {
        if (this.secretaryToDelete) {
            this.secretaries = this.secretaries.filter(s => s.id !== this.secretaryToDelete.id);
        }
        this.isDeleteModalOpen = false;
        this.secretaryToDelete = null;
    }

    saveSecretary() {
        if (!this.secretaryForm.name || !this.secretaryForm.email) return;

        if (this.editingSecretary) {
            // Update
            const index = this.secretaries.findIndex(s => s.id === this.editingSecretary.id);
            if (index !== -1) {
                this.secretaries[index] = { ...this.secretaryForm };
            }
        } else {
            // Create
            const newId = this.secretaries.length > 0 ? Math.max(...this.secretaries.map(s => s.id)) + 1 : 1;
            this.secretaries.push({ ...this.secretaryForm, id: newId });
        }
        this.isModalOpen = false;
        this.resetForm();
    }

    private resetForm() {
        this.secretaryForm = {
            id: 0,
            name: '',
            email: '',
            phone: '',
            address: '',
            status: 'active'
        };
    }
}
