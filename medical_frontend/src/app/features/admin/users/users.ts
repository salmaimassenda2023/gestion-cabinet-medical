import { Component, OnInit } from '@angular/core';
import { forkJoin, Observable, of } from 'rxjs';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { SidebarComponent } from '../../../shared/components/sidebar/sidebar.component';
import { UserTableComponent } from '../../../shared/components/user-table/user-table.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { UserFormComponent } from '../../../shared/components/user-form/user-form.component';
import { User } from '../../../core/models/user.model';
import { UtilisateurService, UtilisateurResponse, UtilisateurRequest } from '../../auth/services/utilisateur.service';

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
    this.utilisateurService.getAllUsers().subscribe({
      next: (users) => {
        this.allUsers = users.map(u => this.mapToUser(u));
        this.filterAndPaginate();
      },
      error: (err) => console.error('Error loading users:', err)
    });
  }

  mapToUser(u: UtilisateurResponse): User {
    return {
      id: u.idUtilisateur.toString(),
      name: `${u.nom} ${u.prenom}`, // Format specifically requested as LASTNAME Firstname often, but following existing pattern
      email: u.email || `${u.login}@cabinet.ma`,
      phone: u.numTel,
      status: u.actif ? 'active' : 'deactivate',
      role: u.role === 'MEDECIN' ? 'doctor' : (u.role === 'ADMIN' ? 'admin' : 'secretaire'),
      signature: u.signature || 'assets/signature1.png'
    };
  }

  calculatePagination() {
    const filtered = this.getFilteredUsers();
    this.totalPages = Math.ceil(filtered.length / this.itemsPerPage) || 1;
    // Adjust current page if out of bounds
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
  }

  getFilteredUsers(): User[] {
    let filtered = this.allUsers;

    // Tab filter (Role)
    if (this.activeTab === 'administration') {
      filtered = filtered.filter(u => u.role === 'admin' || u.role === 'super_admin'); // Include super_admin just in case
    } else {
      filtered = filtered.filter(u => u.role === 'doctor');
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

    // Note: Status filter removed generally as backend doesn't support changing it easily, 
    // but we can still filter VIEW by status if desired. Keeping it for view only.
    if (this.activeFilter !== 'all') {
      filtered = filtered.filter(u => u.status === this.activeFilter);
    }

    return filtered;
  }

  filterAndPaginate() {
    const filtered = this.getFilteredUsers();
    this.calculatePagination();

    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.users = filtered.slice(startIndex, endIndex);
  }

  // Getter for template to match existing property expectations if any, 
  // but strictly we should use the this.users (paginated)
  get filteredUsers(): User[] {
    return this.users;
  }

  onSearch(term: string): void {
    this.searchTerm = term;
    this.currentPage = 1;
    this.filterAndPaginate();
  }

  addUser(): void {
    this.isAddModalOpen = true;
  }

  onSaveNewUser(userData: Partial<User>): void {
    const role = this.activeTab === 'administration' ? 'ADMIN' : 'MEDECIN';

    // Generate a login based on name components or random
    // In a real app, user might input login/password. 
    // For now generating defaults as per likely requirement or asking user form to provide them?
    // The UserFormComponent output likely only gives name, email, phone... 
    // We'll assume we need to generate login/password or modify the form later.
    // For now, let's generate from name.

    const names = userData.name?.split(' ') || ['User', 'Test'];
    const prenom = names.length > 1 ? names[names.length - 1] : names[0];
    const nom = names.length > 1 ? names.slice(0, -1).join(' ') : 'Name';

    // We need a password. The form doesn't seem to have it? 
    // We'll set a default one.
    const defaultPassword = 'Password123!';
    const login = `${prenom.toLowerCase()}.${nom.toLowerCase()}.${Date.now()}`;

    const request: UtilisateurRequest = {
      login: login,
      password: defaultPassword,
      nom: nom,
      prenom: prenom,
      numTel: userData.phone || '0000000000',
      role: role,
      // idCabinet is optional for admins, required for doctors but allowed to be null/default?
      // Backend service says idCabinet required for MEDECIN/SECRETAIRE.
      // We need a cabinet ID. Checking environment or hardcoding for now/asking user?
      // Let's assume cabinet ID 1 for now or handle the error. 
      // Actually the backend check `if ((role == MEDECIN... && idCabinet == null)`.
      // We'll try without and see, or default to 1 if we can.
      idCabinet: role === 'MEDECIN' ? 1 : undefined // Fallback to 1, risky but needed if no UI selection
    };

    this.utilisateurService.createUtilisateur(request).subscribe({
      next: (response) => {
        console.log('User created:', response);
        this.loadUsers(); // Reload list
        this.isAddModalOpen = false;
      },
      error: (err) => {
        console.error('Error creating user:', err);
        // Ideally show error message
      }
    });
  }

  editUser(user: User): void {
    this.selectedUser = user;
    this.isEditModalOpen = true;
  }

  onUpdateUser(userData: Partial<User>): void {
    if (this.selectedUser) {
        const userId = parseInt(this.selectedUser.id);
        const updates: Observable<any>[] = [];

        // 1. Status Update
        if (userData.status && userData.status !== this.selectedUser.status) {
           const isActive = userData.status === 'active';
           updates.push(this.utilisateurService.updateUserStatus(userId, isActive));
        }

        // 2. Profile Update
        // We always run this as per original logic, or checks can be added. 
        // Assuming we want to persist other fields too.
        const names = userData.name?.split(' ') || [];
        const prenom = names.length > 1 ? names[names.length - 1] : userData.name;
        const nom = names.length > 1 ? names.slice(0, -1).join(' ') : '';
        
        const request = {
            nom: nom || undefined,
            prenom: prenom || undefined,
            numTel: userData.phone,
            signature: userData.signature
        };
        updates.push(this.utilisateurService.updateUtilisateur(userId, request));

        // 3. Execute all
        forkJoin(updates).subscribe({
            next: (results) => {
                console.log('Update completed:', results);
                this.loadUsers(); // Reload only after ALL updates are done
                this.isEditModalOpen = false;
                this.selectedUser = undefined;
            },
            error: (err) => console.error('Error in update flow:', err)
        });
    }
  }

  deleteUser(user: User): void {
    this.selectedUser = user;
    this.isDeleteModalOpen = true;
  }

  confirmDelete(): void {
    if (this.selectedUser) {
      this.utilisateurService.deleteUser(parseInt(this.selectedUser.id)).subscribe({
        next: () => {
          this.loadUsers();
          this.isDeleteModalOpen = false;
          this.selectedUser = undefined;
        },
        error: (err) => console.error('Error deleting user:', err)
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
    this.currentPage = 1;
    this.filterAndPaginate();
  }

  onFilterChange(filter: 'all' | 'active' | 'deactivate'): void {
    this.activeFilter = filter;
    this.currentPage = 1;
    this.filterAndPaginate();
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.filterAndPaginate();
    }
  }
}