import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from '../../../shared/components/header/header.component';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { UtilisateurService, UtilisateurResponse, UtilisateurRequest } from '../../auth/services/utilisateur.service';
import { FormsModule } from '@angular/forms';
import { CabinetService } from '../../doctor/services/cabinet.service';
import { switchMap, catchError, tap, finalize, takeUntil } from 'rxjs/operators';
import { of, Subject } from 'rxjs';

@Component({
    selector: 'app-secretary-management',
    standalone: true,
    imports: [CommonModule, RouterModule, HeaderComponent, ModalComponent, FormsModule],
    templateUrl: './secretary-management.html',
    styleUrls: ['./secretary-management.css']
})
export class SecretaryManagementComponent implements OnInit, OnDestroy {
    secretaries: UtilisateurResponse[] = [];
    idCabinet!: number;
    isLoading = true;
    errorMessage = '';
    dataLoaded = false;

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
    };

    private destroy$ = new Subject<void>();

    constructor(
        private utilisateurService: UtilisateurService,
        private cabinetService: CabinetService,
        private cdr: ChangeDetectorRef  
    ) { 
        console.log('🔨 Component constructor called');
    }

    ngOnInit() {
        console.log('🔨 ngOnInit called');
        this.loadData();
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
    }

    trackById(index: number, item: UtilisateurResponse): number {
        return item.idUtilisateur || index;
    }

    loadData() {
        console.log('🚀 loadData() called');
        this.isLoading = true;
        this.errorMessage = '';
        
        this.utilisateurService.getCurrentUser().pipe(
            takeUntil(this.destroy$),
            tap(user => {
                console.log('📋 Current user:', user);
            }),
            switchMap(user => {
                if (!user?.idUtilisateur) {
                    throw new Error('No user ID found');
                }
                return this.cabinetService.getCabinetByMedecinId(user.idUtilisateur);
            }),
            switchMap(cabinet => {
                console.log('📦 Cabinet:', cabinet);
                
                if (!cabinet?.id) {
                    throw new Error('No cabinet found');
                }
                
                this.idCabinet = cabinet.id;
                return this.utilisateurService.getUtilisateursByCabinet(this.idCabinet);
            }),
            catchError(err => {
                console.error('❌ Error:', err);
                this.errorMessage = err.message || 'Error loading data';
                return of([]);
            }),
            finalize(() => {
                this.isLoading = false;
                this.cdr.detectChanges(); 
                console.log('🔄 Change detection triggered');
            })
        ).subscribe({
            next: (users) => {
                console.log('✅ Users received:', users);
                this.secretaries = (users || []).filter(u => u?.role === 'SECRETAIRE');
                console.log('✅ Secretaries filtered:', this.secretaries.length);
            },
            error: (err) => {
                console.error('❌ Subscription error:', err);
                this.errorMessage = 'Failed to load data';
            }
        });
    }



    private loadSecretariesData() {
        if (!this.idCabinet) {
            return of([]);
        }
        
        return this.utilisateurService.getUtilisateursByCabinet(this.idCabinet).pipe(
            tap(users => {
                console.log('✅ Raw users from API:', users);
            }),
            catchError(err => {
                console.error('❌ Error fetching users:', err);
                this.errorMessage = 'Error loading secretaries.';
                return of([]);
            })
        );
    }

    loadSecretaries() {
        this.isLoading = true;
        this.errorMessage = '';
        
        this.loadSecretariesData().pipe(
            finalize(() => {
                this.isLoading = false;
            })
        ).subscribe({
            next: (users) => {
                this.secretaries = users.filter((u: UtilisateurResponse) => u.role === 'SECRETAIRE');
                console.log('✅ Loaded secretaries:', this.secretaries);
            },
            error: (err) => {
                console.error('❌ Error in loadSecretaries:', err);
                this.errorMessage = 'Error loading secretaries.';
            }
        });
    }

    addSecretary() {
        if (!this.idCabinet) {
            alert('Cabinet ID not available. Please refresh the page.');
            return;
        }
        this.editingSecretary = null;
        this.resetForm();
        this.isModalOpen = true;
    }

    editSecretary(sec: UtilisateurResponse) {
        this.editingSecretary = { ...sec };
        this.secretaryForm = {
            login: sec.login,
            password: '',
            nom: sec.nom,
            prenom: sec.prenom,
            numTel: sec.numTel,
        };
        this.isModalOpen = true;
    }

    confirmDelete(sec: UtilisateurResponse) {
        this.secretaryToDelete = sec;
        this.isDeleteModalOpen = true;
    }

    onConfirmDelete() {
        if (this.secretaryToDelete && this.secretaryToDelete.idUtilisateur) {
            this.isLoading = true;
            this.utilisateurService.deleteUser(this.secretaryToDelete.idUtilisateur).pipe(
                finalize(() => {
                    this.isLoading = false;
                })
            ).subscribe({
                next: () => {
                    this.loadSecretaries();
                    this.isDeleteModalOpen = false;
                    this.secretaryToDelete = null;
                },
                error: (err) => {
                    console.error('❌ Error deleting secretary:', err);
                    this.errorMessage = 'Error deleting secretary.';
                }
            });
        }
    }

    onCancelDelete() {
        this.isDeleteModalOpen = false;
        this.secretaryToDelete = null;
    }

    saveSecretary() {
        if (!this.secretaryForm.login || !this.secretaryForm.nom || !this.secretaryForm.prenom || !this.secretaryForm.numTel) {
            alert('Please fill in all required fields: Login, First Name, Last Name, and Phone Number');
            return;
        }

        if (!this.idCabinet) {
            alert('Error: Cabinet ID not found. Please refresh and try again.');
            return;
        }
        const currentCabinetId = this.idCabinet;

        this.isLoading = true;
        
        if (this.editingSecretary && this.editingSecretary.idUtilisateur) {
            const updateRequest = {
                nom: this.secretaryForm.nom,
                prenom: this.secretaryForm.prenom,
                numTel: this.secretaryForm.numTel
            };
            
            this.utilisateurService.updateUtilisateur(this.editingSecretary.idUtilisateur, updateRequest).pipe(
                finalize(() => {
                    this.isLoading = false;
                })
            ).subscribe({
                next: () => {
                    this.loadSecretaries();
                    this.isModalOpen = false;
                    this.resetForm();
                },
                error: (err) => {
                    console.error('❌ Error updating secretary:', err);
                    this.errorMessage = 'Error updating secretary.';
                }
            });
        } else {
            const newSecretary: UtilisateurRequest = {
                login: this.secretaryForm.login,
                password: this.secretaryForm.password,
                nom: this.secretaryForm.nom,
                prenom: this.secretaryForm.prenom,
                numTel: this.secretaryForm.numTel,
                role: 'SECRETAIRE',
                signature: '',
                idCabinet: currentCabinetId
            };

            console.log('📝 Creating secretary with data:', newSecretary);
            console.log('📝 Using cabinet ID:', currentCabinetId);

            this.utilisateurService.createUtilisateur(newSecretary).pipe(
                finalize(() => {
                    this.isLoading = false;
                })
            ).subscribe({
                next: (response) => {
                    console.log('✅ Secretary created successfully:', response);
                    this.idCabinet = currentCabinetId;
                    this.loadSecretaries();
                    this.isModalOpen = false;
                    this.resetForm();
                },
                error: (err) => {
                    console.error('❌ Error creating secretary:', err);
                    console.error('❌ Error details:', err.error);
                    this.errorMessage = 'Error creating secretary. Please check the console for details.';
                }
            });
        }
    }

    refreshData() {
        this.loadData();
    }

    private resetForm() {
        this.secretaryForm = {
            login: '',
            password: '',
            nom: '',
            prenom: '',
            numTel: '',
        };
    }
}