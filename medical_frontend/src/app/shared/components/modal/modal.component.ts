import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-modal',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.css']
})
export class ModalComponent {
    @Input() isOpen: boolean = false;
    @Input() title: string = '';
    @Input() showFooter: boolean = true;
    @Input() confirmText: string = 'Confirm';
    @Input() cancelText: string = 'Cancel';
    @Input() isDestructive: boolean = false;

    @Output() close = new EventEmitter<void>();
    @Output() confirm = new EventEmitter<void>();

    onClose(): void {
        this.close.emit();
    }

    onConfirm(): void {
        this.confirm.emit();
    }
}
