import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { User } from '../../../core/models/user.model';


@Component({
    selector: 'app-user-table',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './user-table.component.html',
    styleUrls: ['./user-table.component.css']
})
export class UserTableComponent {
    @Input() users: User[] = [];
    @Input() currentPage: number = 1;
    @Input() totalPages: number = 1;
    @Input() activeTab: 'administration' | 'doctors' = 'administration';
    @Input() activeFilter: 'all' | 'active' | 'deactivate' = 'all';

    @Output() add = new EventEmitter<void>();
    @Output() edit = new EventEmitter<User>();
    @Output() delete = new EventEmitter<User>();
    @Output() pageChange = new EventEmitter<number>();
    @Output() tabChange = new EventEmitter<'administration' | 'doctors'>();
    @Output() filterChange = new EventEmitter<'all' | 'active' | 'deactivate'>();

    showFilterMenu = false;

    getStatusClass(status: 'active' | 'deactivate'): string {
        return status === 'active' ? 'status-active' : 'status-deactivate';
    }

    getStatusText(status: 'active' | 'deactivate'): string {
        return status === 'active' ? 'Active' : 'Deactivate';
    }

    onAdd(): void {
        this.add.emit();
    }

    onEdit(user: User): void {
        this.edit.emit(user);
    }

    onDelete(user: User): void {
        this.delete.emit(user);
    }

    previousPage(): void {
        if (this.currentPage > 1) {
            this.pageChange.emit(this.currentPage - 1);
        }
    }

    nextPage(): void {
        if (this.currentPage < this.totalPages) {
            this.pageChange.emit(this.currentPage + 1);
        }
    }

    setTab(tab: 'administration' | 'doctors'): void {
        this.tabChange.emit(tab);
    }

    setFilter(filter: 'all' | 'active' | 'deactivate'): void {
        this.activeFilter = filter;
        this.filterChange.emit(filter);
        this.showFilterMenu = false;
    }

    toggleFilterMenu(): void {
        this.showFilterMenu = !this.showFilterMenu;
    }

    getFilterLabel(): string {
        switch (this.activeFilter) {
            case 'active': return 'Active';
            case 'deactivate': return 'Deactivate';
            default: return 'All Status';
        }
    }
}
