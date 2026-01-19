import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { AppointmentFormComponent } from './appointment-form/appointment-form.component';
import { RendezvousService } from '../../../core/services/rendezvous.service';
import { RendezVous, StatutRendezVous, MotifRendezVous } from '../../../core/models/rendezvous.model';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { PatientService } from '../../../core/services/patient.service';
import { CabinetService } from '../../doctor/services/cabinet.service';
import { forkJoin, Subject, of } from 'rxjs';
import { catchError, finalize, switchMap, takeUntil, tap } from 'rxjs/operators';

@Component({
    selector: 'app-secretary-calendar',
    standalone: true,
    imports: [CommonModule, ModalComponent, AppointmentFormComponent],
    templateUrl: './calendar.html',
    styleUrls: ['./calendar.css']
})
export class SecretaryCalendarComponent implements OnInit, OnDestroy {
    currentDate = new Date();
    days: number[] = [];
    weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
    isModalOpen = false;
    isDeleteModalOpen = false;
    selectedAppointment?: RendezVous;

    activeTypeFilter: string = 'all';
    activeStatusFilter: string = 'all';

    appointments: RendezVous[] = [];
    idCabinet?: number;
    medecins: any[] = [];
    selectedMedecinId?: number;
    selectedDay?: number;

    isLoading = true;
    errorMessage = '';
    private destroy$ = new Subject<void>();

    constructor(
        private rendezvousService: RendezvousService,
        private utilisateurService: UtilisateurService,
        private patientService: PatientService,
        private cabinetService: CabinetService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit() {
        console.log('📅 Calendar component initialized');
        this.generateCalendar();
        this.loadInitialData();
    }

    ngOnDestroy() {
        this.destroy$.next();
        this.destroy$.complete();
    }

loadInitialData() {
    this.isLoading = true;
    this.errorMessage = '';

    // First get the current user
    this.utilisateurService.getCurrentUser().pipe(
        takeUntil(this.destroy$),
        switchMap(currentUser => {
            console.log('👤 Current user:', currentUser);
            
            return this.cabinetService.getCabinetByMedecinId(currentUser.idUtilisateur).pipe(
                catchError(err => {
                    console.error('❌ Error fetching cabinet for medecin:', err);
                    this.errorMessage = 'Error loading cabinet information';
                    this.isLoading = false;
                    return of(null);
                })
            );
        }),
        catchError(err => {
            console.error('❌ Error fetching current user:', err);
            this.errorMessage = 'Error loading user information';
            this.isLoading = false;
            return of(null);
        })
    ).subscribe({
        next: (cabinet) => {
            if (cabinet?.id) {
                this.idCabinet = cabinet.id;
                console.log('✅ Cabinet ID for calendar:', this.idCabinet);
                this.loadMedecinsAndAppointments();
            } else {
                this.errorMessage = 'No cabinet found';
                this.isLoading = false;
            }
        },
        error: (err) => {
            console.error('❌ Error in cabinet subscription:', err);
            this.errorMessage = 'Error loading cabinet information';
            this.isLoading = false;
        }
    });
}
    loadMedecinsAndAppointments() {
        if (!this.idCabinet) {
            this.errorMessage = 'Cabinet ID not available';
            this.isLoading = false;
            return;
        }

        this.utilisateurService.getUtilisateursByCabinet(this.idCabinet).pipe(
            takeUntil(this.destroy$),
            finalize(() => {
                this.isLoading = false;
                this.cdr.detectChanges();
            })
        ).subscribe({
            next: (cabinetUsers) => {
                console.log('👨‍⚕️ Cabinet users loaded:', cabinetUsers);

                cabinetUsers.forEach((user: any) => {
                    console.log(`Cabinet User ${user.idUtilisateur}: ${user.prenom} ${user.nom}, Role: "${user.role}"`);
                });

                // 1. D'abord, ajouter l'utilisateur courant s'il a le rôle MEDECIN
                this.utilisateurService.getCurrentUser().pipe(
                    takeUntil(this.destroy$)
                ).subscribe(currentUser => {
                    console.log('👤 Current user:', currentUser);

                    let allPotentialMedecins = [...cabinetUsers];

                    // Ajouter l'utilisateur courant s'il a le rôle MEDECIN et n'est pas déjà dans la liste
                    if (currentUser.role === 'MEDECIN' &&
                        !allPotentialMedecins.some(u => u.idUtilisateur === currentUser.idUtilisateur)) {
                        console.log('➕ Adding current user (MEDECIN) to medecins list');
                        allPotentialMedecins.push(currentUser);
                    }

                    // Maintenant filtrer pour trouver les médecins
                    this.medecins = allPotentialMedecins.filter((u: any) => {
                        const userRole = u.role?.toUpperCase?.() || u.role || '';
                        console.log(`Checking user ${u.idUtilisateur}: role="${u.role}", uppercase="${userRole}"`);
                        return userRole === 'MEDECIN';
                    });

                    console.log('✅ Final medecins list:', this.medecins);

                    if (this.medecins.length > 0) {
                        this.selectedMedecinId = this.medecins[0].idUtilisateur;
                        console.log('🎯 Selected medecin ID:', this.selectedMedecinId);
                        this.loadAppointments();
                    } else {
                        console.log('⚠️ No medecins found, using current user as fallback');
                        this.medecins = [currentUser];
                        this.selectedMedecinId = currentUser.idUtilisateur;
                        this.loadAppointments();
                    }
                });
            },
            error: (err) => {
                console.error('❌ Error fetching cabinet users:', err);
                this.errorMessage = 'Error loading cabinet users';
            }
        });
    }

    loadAppointments() {
        // By default, load appointments for today
        const today = new Date().getDate();

        // Check if today is in the current month view
        const currentMonth = this.currentDate.getMonth();
        const todayMonth = new Date().getMonth();
        const currentYear = this.currentDate.getFullYear();
        const todayYear = new Date().getFullYear();

        if (currentMonth === todayMonth && currentYear === todayYear) {
            // If we're viewing the current month, select today
            this.selectedDay = today;
            this.loadAppointmentsForDay(today);
        } else {
            // If viewing a different month, don't select any day
            this.selectedDay = undefined;
            this.appointments = []; // Clear appointments
            console.log('📅 Viewing different month, no day selected');
        }
    }

    get filteredAppointments(): RendezVous[] {
        return this.appointments.filter(appt => {
            const typeMatch = this.activeTypeFilter === 'all' || appt.motif === this.activeTypeFilter;
            const statusMatch = this.activeStatusFilter === 'all' || appt.statut === this.activeStatusFilter;
            return typeMatch && statusMatch;
        });
    }

    generateCalendar() {
        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        this.days = Array.from({ length: daysInMonth }, (_, i) => i + 1);
    }

    get monthName(): string {
        return this.currentDate.toLocaleString('default', { month: 'long' });
    }

    nextMonth() {
        this.currentDate = new Date(this.currentDate.setMonth(this.currentDate.getMonth() + 1));
        this.generateCalendar();
        this.selectedDay = undefined; 
        this.appointments = []; 
    }

    previousMonth() {
        this.currentDate = new Date(this.currentDate.setMonth(this.currentDate.getMonth() - 1));
        this.generateCalendar();
        this.selectedDay = undefined; 
        this.appointments = []; 
    }

    openAddModal() {
        if (!this.selectedMedecinId) {
            alert('Please select a medecin first');
            return;
        }
        this.selectedAppointment = undefined;
        this.isModalOpen = true;
    }

    editAppointment(appt: RendezVous) {
        this.selectedAppointment = appt;
        this.isModalOpen = true;
    }

    closeAddModal() {
        this.isModalOpen = false;
        this.selectedAppointment = undefined;
    }

    onSaveAppointment(data: any) {
        console.log('💾 Saving appointment data:', data);

        if (!this.selectedMedecinId && !this.selectedAppointment) {
            alert('Error: No medecin selected');
            return;
        }

        let hour = data.hour;
        if (hour.length === 5) { 
            hour = hour + ':00';
        }

        if (this.selectedAppointment && this.selectedAppointment.id) {
            const updateDto = {
                dateRdv: data.date,
                heureRdv: hour,
                motif: data.type
            };

            console.log('🔄 Updating appointment:', updateDto);

            this.rendezvousService.updateRendezVous(this.selectedAppointment.id, updateDto).subscribe({
                next: (updatedAppt) => {
                    console.log('✅ Appointment updated successfully');

                    if (data.statut && data.statut !== this.selectedAppointment?.statut) {
                        console.log('🔄 Updating status to:', data.statut);
                        this.rendezvousService.changeStatut(this.selectedAppointment?.id!, data.statut).subscribe({
                            next: () => {
                                console.log('✅ Status updated successfully');
                                this.loadAppointments();
                                this.closeAddModal();
                            },
                            error: (err) => {
                                console.error('❌ Error updating status:', err);
                                alert('Appointment updated but status change failed. Please check console.');
                                this.loadAppointments();
                                this.closeAddModal();
                            }
                        });
                    } else {
                        this.loadAppointments();
                        this.closeAddModal();
                    }
                },
                error: (err) => {
                    console.error('❌ Error updating appointment:', err);
                    alert('Error updating appointment. Please check console for details.');
                }
            });
        } else {
            const patientId = Number(data.patientId);
            if (isNaN(patientId)) {
                alert('Error: Invalid patient ID');
                return;
            }

            const createDto = {
                idPatient: patientId,
                idMedecin: this.selectedMedecinId!,
                dateRdv: data.date,
                heureRdv: hour,
                motif: data.type
            };

            console.log('➕ Creating appointment with PLANIFIE status:', createDto);

            this.rendezvousService.createRendezVous(createDto).subscribe({
                next: (response) => {
                    console.log('✅ Appointment created successfully with status:', response.statut);
                    this.loadAppointments();
                    this.closeAddModal();
                },
                error: (err) => {
                    console.error('❌ Error creating appointment:', err);
                    console.error('❌ Error details:', err.error);
                    alert('Error creating appointment. Please check console for details.');
                }
            });
        }
    }
    onDeleteAppointment() {
        this.isModalOpen = false;
        this.isDeleteModalOpen = true;
    }

    confirmDelete() {
        if (this.selectedAppointment && this.selectedAppointment.id) {
            this.rendezvousService.deleteRendezVous(this.selectedAppointment.id).subscribe({
                next: () => {
                    console.log('🗑️ Appointment deleted successfully');
                    this.loadAppointments();
                    this.isDeleteModalOpen = false;
                    this.selectedAppointment = undefined;
                },
                error: (err) => {
                    console.error('❌ Error deleting appointment:', err);
                    alert('Error deleting appointment.');
                }
            });
        }
    }

    setTypeFilter(type: string) {
        this.activeTypeFilter = type;
    }

    setStatusFilter(status: string) {
        this.activeStatusFilter = status;
    }

    onMedecinChange(event: any) {
        this.selectedMedecinId = +event.target.value;
        console.log('🔄 Medecin changed to:', this.selectedMedecinId);
        this.loadAppointments();
    }
    hasAppointmentsOnDay(day: number): boolean {
        if (!this.appointments.length) return false;

        const dateStr = `${this.currentDate.getFullYear()}-${String(this.currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        return this.appointments.some(appt => {
            const apptDate = appt.dateRdv ? new Date(appt.dateRdv).toISOString().split('T')[0] : '';
            return apptDate === dateStr;
        });
    }
    onDayClick(day: number) {
        console.log('📅 Day clicked:', day);
        this.selectedDay = day;

        // Load appointments for the selected day
        this.loadAppointmentsForDay(day);
    }

    loadAppointmentsForDay(day: number) {
        if (!this.selectedMedecinId) {
            console.error('❌ Cannot load appointments: No medecin selected');
            return;
        }

        const year = this.currentDate.getFullYear();
        const month = this.currentDate.getMonth() + 1;
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

        console.log('📅 Loading appointments for:', dateStr, 'Medecin:', this.selectedMedecinId);

        this.isLoading = true;

        forkJoin({
            rendezvous: this.rendezvousService.getRendezVousByMedecinAndDate(this.selectedMedecinId, dateStr).pipe(
                catchError(err => {
                    console.error('❌ Error fetching rendezvous:', err);
                    return of([]);
                })
            ),
            patients: this.idCabinet ? this.patientService.getPatientsByCabinet(this.idCabinet).pipe(
                catchError(err => {
                    console.error('❌ Error fetching patients:', err);
                    return of([]);
                })
            ) : of([])
        }).pipe(
            takeUntil(this.destroy$),
            finalize(() => {
                this.isLoading = false;
                this.cdr.detectChanges();
            })
        ).subscribe({
            next: ({ rendezvous, patients }) => {
                console.log('📊 Appointments for selected day:', rendezvous);
                console.log('👥 Patients loaded:', patients.length);

                this.appointments = rendezvous.map(rdv => {
                    const patient = patients.find((p: any) => p.id === rdv.idPatient);
                    return {
                        ...rdv,
                        patientName: patient ? `${patient.prenom} ${patient.nom}` : `Patient #${rdv.idPatient}`
                    };
                });

                console.log(`✅ ${this.appointments.length} appointments loaded for ${dateStr}`);
            },
            error: (err) => {
                console.error('❌ Error loading appointments:', err);
                this.errorMessage = 'Error loading appointments';
            }
        });
    }
}