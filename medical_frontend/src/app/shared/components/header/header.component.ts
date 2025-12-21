import { RouterModule } from '@angular/router';
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-header',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.css']
})
export class HeaderComponent {
    @Input() logoPath: string = 'assets/logo.png';
    @Input() role: 'admin' | 'secretary' = 'admin';
    @Output() logout = new EventEmitter<void>();
    @Output() searchChange = new EventEmitter<string>();

    get logoLink(): string {
        return this.role === 'admin' ? '/admin' : '/secretary';
    }

    get profileLink(): string {
        return this.role === 'admin' ? '/admin/profile' : '/secretary/profile';
    }

    notifications = [
        { id: 1, text: 'New appointment scheduled for tomorrow', time: '5m ago' },
        { id: 2, text: 'Clinic "Heart Care" updated its profile', time: '1h ago' },
        { id: 3, text: 'Monthly payment for Medical Center processed', time: '2h ago' }
    ];

    showNotifications = false;

    onLogout(): void {
        this.logout.emit();
    }

    onSearch(event: any): void {
        this.searchChange.emit(event.target.value);
    }

    toggleNotifications(): void {
        this.showNotifications = !this.showNotifications;
    }

    deleteNotification(id: number, event: Event): void {
        event.stopPropagation();
        this.notifications = this.notifications.filter(n => n.id !== id);
    }
}
