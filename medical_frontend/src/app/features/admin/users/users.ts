import { Component, OnInit, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { UserTableComponent } from '../../../shared/components/user-table/user-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { UserFormComponent } from '../../../shared/components/user-form/user-form.component';
import { User } from '../../../core/models/user.model';


@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    HeaderComponent,
    SidebarComponent,
    UserTableComponent,
    ModalComponent,
    UserFormComponent
  ],
  templateUrl: './users.html',
  styleUrls: ['./users.css']
})
export class UsersComponent implements OnInit {
  logoPath = 'assets/logo.png';
  users: User[] = [
    { id: '00001', name: 'Peter Mullin', email: 'email@exemple.com', phone: '+212 76382652', status: 'active', signature: 'assets/signature1.png', role: 'admin' },
    { id: '00002', name: 'Andrew Kim', email: 'email@exemple.com', phone: '+212 76382652', status: 'active', role: 'doctor' },
    { id: '00003', name: 'Bob Fisher', email: 'email@exemple.com', phone: '+212 76382652', status: 'deactivate', role: 'admin' },
    { id: '00004', name: 'Tom Young', email: 'email@exemple.com', phone: '+212 76382652', status: 'active', role: 'doctor' },
    { id: '00005', name: 'Sandra Bay', email: 'email@exemple.com', phone: '+212 76382652', status: 'active', role: 'admin' },
    { id: '00006', name: 'Alfred Murray', email: 'email@exemple.com', phone: '+212 76382652', status: 'active', role: 'doctor' }
  ];

  activeTab: 'administration' | 'doctors' = 'administration';
  activeFilter: 'all' | 'active' | 'deactivate' = 'all';
  searchTerm: string = '';

  // Modals state
  isAddModalOpen = false;
  isEditModalOpen = false;
  isDeleteModalOpen = false;
  isLogoutModalOpen = false;

  selectedUser?: User;

  currentPage = 1;
  itemsPerPage = 5;
  totalPages = 5; // Updated totalPages

  ngOnInit(): void { }

  get filteredUsers(): User[] {
    let filtered = this.users;

    // Tab filter
    filtered = filtered.filter(u => u.role === (this.activeTab === 'administration' ? 'admin' : 'doctor'));

    // Status filter
    if (this.activeFilter !== 'all') {
      // Note: The instruction uses 'Active'/'Deactivate' (capitalized) for filtering,
      // but the User interface defines 'active'/'deactivate' (lowercase).
      // Assuming the instruction intends to filter against the lowercase status values.
      filtered = filtered.filter(u => u.status === (this.activeFilter === 'active' ? 'active' : 'deactivate'));
    }

    // Search filter
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(u =>
        u.name.toLowerCase().includes(term) ||
        u.email.toLowerCase().includes(term) ||
        u.phone.toLowerCase().includes(term) ||
        u.id.toLowerCase().includes(term)
      );
    }

    return filtered;
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.currentPage = 1;
  }

  addUser(): void {
    this.isAddModalOpen = true;
  }

  onSaveNewUser(userData: Partial<User>): void {
    const newUser: User = {
      id: `0000${this.users.length + 1}`,
      name: userData.name!,
      email: userData.email!,
      phone: userData.phone!,
      status: userData.status as any,
      role: this.activeTab === 'administration' ? 'admin' : 'doctor',
      signature: userData.signature
    };
    this.users = [...this.users, newUser];
    this.isAddModalOpen = false;
  }

  editUser(user: User): void {
    this.selectedUser = user;
    this.isEditModalOpen = true;
  }

  onUpdateUser(userData: Partial<User>): void {
    if (this.selectedUser) {
      const index = this.users.findIndex(u => u.id === this.selectedUser?.id);
      if (index > -1) {
        this.users[index] = { ...this.selectedUser, ...userData };
        this.users = [...this.users];
      }
      this.isEditModalOpen = false;
      this.selectedUser = undefined;
    }
  }

  deleteUser(user: User): void {
    this.selectedUser = user;
    this.isDeleteModalOpen = true;
  }

  confirmDelete(): void {
    if (this.selectedUser) {
      this.users = this.users.filter(u => u.id !== this.selectedUser?.id);
      this.isDeleteModalOpen = false;
      this.selectedUser = undefined;
    }
  }

  logout(): void {
    this.isLogoutModalOpen = true;
  }

  confirmLogout(): void {
    console.log('Logging out...');
    this.isLogoutModalOpen = false;
    // Implement redirect to login
  }

  onTabChange(tab: 'administration' | 'doctors'): void {
    this.activeTab = tab;
  }

  onFilterChange(filter: 'all' | 'active' | 'deactivate'): void {
    this.activeFilter = filter;
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }
}