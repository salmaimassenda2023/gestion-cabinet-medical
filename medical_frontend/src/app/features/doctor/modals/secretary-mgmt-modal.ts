import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';

@Component({
    selector: 'app-secretary-mgmt-modal',
    standalone: true,
    imports: [CommonModule, ModalComponent],
    templateUrl: './secretary-mgmt-modal.html',
    styleUrls: ['./secretary-mgmt-modal.css']
})
export class SecretaryMgmtModalComponent implements OnInit {
    @Output() close = new EventEmitter<void>();

    secretaries = [
        { id: 1, name: 'Alice Smith', email: 'alice@example.com', phone: '123-456-7890' },
        { id: 2, name: 'Bob Jones', email: 'bob@example.com', phone: '098-765-4321' }
    ];

    ngOnInit() { }

    onClose() {
        this.close.emit();
    }

    addSecretary() {
        console.log('Adding secretary...');
    }

    editSecretary(sec: any) {
        console.log('Editing secretary:', sec);
    }

    deleteSecretary(id: number) {
        this.secretaries = this.secretaries.filter(s => s.id !== id);
    }
}
