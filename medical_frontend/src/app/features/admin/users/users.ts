import { Component, OnInit, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { UserTableComponent } from '../../../shared/components/user-table/user-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { UserFormComponent } from '../../../shared/components/user-form/user-form.component';
import { User } from '../../../core/models/user.model';
import { UtilisateurService, UtilisateurResponse } from '../../auth/services/utilisateur.service';


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
  users: User[] = [];
  allUsers: User[] = [];

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

  totalPages = 1;

  constructor(private utilisateurService: UtilisateurService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers() {
    this.utilisateurService.getAllUsers().subscribe(users => {
      this.allUsers = users.map(u => ({
        id: u.idUtilisateur.toString(),
        name: `${u.prenom} ${u.nom}`,
        email: u.email || 'N/A', // Assuming email might be missing in response type for now
        phone: u.numTel,
        status: u.actif ? 'active' : 'deactivate',
        role: u.role.toLowerCase() as 'admin' | 'doctor' | 'secretaire',
        signature: 'assets/signature1.png' // Placeholder
      }));
      this.users = [...this.allUsers];
      this.calculatePagination();
    });
  }

  calculatePagination() {
    this.totalPages = Math.ceil(this.filteredUsers.length / this.itemsPerPage) || 1;
  }

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
      // Optimistic update
      const index = this.users.findIndex(u => u.id === this.selectedUser?.id);
      if (index > -1) {
        // Check if status changed
        if (userData.status && userData.status !== this.selectedUser.status) {
          const newStatus = userData.status === 'active';
          this.utilisateurService.updateUserStatus(parseInt(this.selectedUser.id), newStatus).subscribe();
        }
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
      this.utilisateurService.deleteUser(parseInt(this.selectedUser.id)).subscribe(() => {
        this.users = this.users.filter(u => u.id !== this.selectedUser?.id);
        this.allUsers = this.allUsers.filter(u => u.id !== this.selectedUser?.id);
        this.isDeleteModalOpen = false;
        this.selectedUser = undefined;
        this.calculatePagination();
      });
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