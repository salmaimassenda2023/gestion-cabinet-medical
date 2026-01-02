import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { AppointmentFormComponent } from './appointment-form/appointment-form.component';
import { RendezvousService } from '../../../core/services/rendezvous.service';
import { RendezVous, StatutRendezVous, MotifRendezVous } from '../../../core/models/rendezvous.model';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { PatientService } from '../../../core/services/patient.service';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-secretary-calendar',
    standalone: true,
    imports: [CommonModule, ModalComponent, AppointmentFormComponent],
    templateUrl: './calendar.html',
    styleUrls: ['./calendar.css']
})
export class SecretaryCalendarComponent implements OnInit {
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

    constructor(
        private rendezvousService: RendezvousService,
        private utilisateurService: UtilisateurService,
        private patientService: PatientService
    ) { }

    ngOnInit() {
        this.generateCalendar();
        this.loadInitialData();
    }

    loadInitialData() {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user) => {
                this.idCabinet = user.idCabinet;
                if (this.idCabinet) {
                    this.loadMedecinsAndAppointments();
                }
            },
            error: (err) => console.error('Error fetching current user:', err)
        });
    }

    loadMedecinsAndAppointments() {
        if (!this.idCabinet) return;

        this.utilisateurService.getUtilisateursByCabinet(this.idCabinet).subscribe({
            next: (users) => {
                this.medecins = users.filter(u => u.role === 'MEDECIN');
                if (this.medecins.length > 0) {
                    this.selectedMedecinId = this.medecins[0].idUtilisateur;
                    this.loadAppointments();
                }
            },
            error: (err) => console.error('Error fetching medecins:', err)
        });
    }

    loadAppointments() {
        if (!this.selectedMedecinId) return;

        const dateStr = this.currentDate.toISOString().split('T')[0];

        forkJoin({
            rendezvous: this.rendezvousService.getRendezVousByMedecinAndDate(this.selectedMedecinId, dateStr),
            patients: this.patientService.getPatientsByCabinet(this.idCabinet!)
        }).subscribe({
            next: ({ rendezvous, patients }) => {
                this.appointments = rendezvous.map(rdv => ({
                    ...rdv,
                    patientName: patients.find(p => p.id === rdv.idPatient)?.nom + ' ' + patients.find(p => p.id === rdv.idPatient)?.prenom
                }));
            },
            error: (err) => console.error('Error fetching appointments:', err)
        });
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
        this.loadAppointments();
    }

    previousMonth() {
        this.currentDate = new Date(this.currentDate.setMonth(this.currentDate.getMonth() - 1));
        this.generateCalendar();
        this.loadAppointments();
    }

    openAddModal() {
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
        if (this.selectedAppointment && this.selectedAppointment.id) {
            // Update
            const updateDto = {
                dateRdv: data.date,
                heureRdv: data.hour.length === 5 ? data.hour + ':00' : data.hour,
                motif: data.type as MotifRendezVous
            };
            this.rendezvousService.updateRendezVous(this.selectedAppointment.id, updateDto).subscribe({
                next: () => {
                    this.loadAppointments();
                    this.closeAddModal();
                },
                error: (err) => console.error('Error updating appointment:', err)
            });
        } else {
            // Create
            const createDto = {
                idPatient: +data.patientId,
                idMedecin: this.selectedMedecinId!,
                dateRdv: data.date,
                heureRdv: data.hour.length === 5 ? data.hour + ':00' : data.hour,
                motif: data.type as MotifRendezVous
            };
            this.rendezvousService.createRendezVous(createDto).subscribe({
                next: () => {
                    this.loadAppointments();
                    this.closeAddModal();
                },
                error: (err) => console.error('Error creating appointment:', err)
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
                    this.loadAppointments();
                    this.isDeleteModalOpen = false;
                    this.selectedAppointment = undefined;
                },
                error: (err) => console.error('Error deleting appointment:', err)
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
        this.loadAppointments();
    }
}
