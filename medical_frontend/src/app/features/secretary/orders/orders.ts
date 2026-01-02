import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RendezvousService } from '../../../core/services/rendezvous.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { PatientService } from '../../../core/services/patient.service';
import { RendezVous, StatutRendezVous } from '../../../core/models/rendezvous.model';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-secretary-orders',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './orders.html',
    styleUrls: ['./orders.css']
})
export class SecretaryOrdersComponent implements OnInit {
    waitingList: RendezVous[] = [];
    todayAppointments: RendezVous[] = [];
    nextPatient?: RendezVous;

    idCabinet?: number;
    medecins: any[] = [];
    selectedMedecinId?: number;

    constructor(
        private rendezvousService: RendezvousService,
        private utilisateurService: UtilisateurService,
        private patientService: PatientService
    ) { }

    ngOnInit() {
        this.loadInitialData();
    }

    loadInitialData() {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user) => {
                this.idCabinet = user.idCabinet;
                if (this.idCabinet) {
                    this.loadMedecins();
                }
            },
            error: (err) => console.error('Error fetching current user:', err)
        });
    }

    loadMedecins() {
        if (!this.idCabinet) return;
        this.utilisateurService.getUtilisateursByCabinet(this.idCabinet).subscribe({
            next: (users) => {
                this.medecins = users.filter(u => u.role === 'MEDECIN');
                if (this.medecins.length > 0) {
                    this.selectedMedecinId = this.medecins[0].idUtilisateur;
                    this.refreshAllData();
                }
            },
            error: (err) => console.error('Error fetching medecins:', err)
        });
    }

    refreshAllData() {
        if (!this.selectedMedecinId) return;

        forkJoin({
            daily: this.rendezvousService.getRendezVousDuJour(this.selectedMedecinId),
            waiting: this.rendezvousService.getListeAttente(this.selectedMedecinId),
            patients: this.patientService.getPatientsByCabinet(this.idCabinet!)
        }).subscribe({
            next: ({ daily, waiting, patients }) => {
                // Map patient names
                const mapPatientName = (rdv: RendezVous) => {
                    const p = patients.find(pat => pat.id === rdv.idPatient);
                    return p ? `${p.nom} ${p.prenom}` : 'Unknown Patient';
                };

                this.todayAppointments = daily.filter(rdv => rdv.statut !== StatutRendezVous.TERMINE && rdv.statut !== StatutRendezVous.PRESENT)
                    .map(rdv => ({ ...rdv, patientName: mapPatientName(rdv) }));

                this.waitingList = waiting.map(rdv => ({ ...rdv, patientName: mapPatientName(rdv) }));

                this.loadNextPatient(patients);
            },
            error: (err) => console.error('Error refreshing data:', err)
        });
    }

    loadNextPatient(patients: any[]) {
        if (!this.selectedMedecinId) return;
        this.rendezvousService.getPatientSuivant(this.selectedMedecinId).subscribe({
            next: (rdv) => {
                const p = patients.find(pat => pat.id === rdv.idPatient);
                this.nextPatient = { ...rdv, patientName: p ? `${p.nom} ${p.prenom}` : 'Unknown Patient' };
            },
            error: (err) => {
                this.nextPatient = undefined;
                console.log('No next patient or error:', err);
            }
        });
    }

    markAsArrived(rdv: RendezVous) {
        if (rdv.id) {
            this.rendezvousService.ajouterEnListeAttente(rdv.id).subscribe({
                next: () => this.refreshAllData(),
                error: (err) => console.error('Error adding to waiting list:', err)
            });
        }
    }

    onMedecinChange(event: any) {
        this.selectedMedecinId = +event.target.value;
        this.refreshAllData();
    }

    onNext() {
        if (this.nextPatient && this.nextPatient.id) {
            this.rendezvousService.changeStatut(this.nextPatient.id, StatutRendezVous.TERMINE).subscribe({
                next: () => this.refreshAllData(),
                error: (err) => console.error('Error completing consultation:', err)
            });
        }
    }
}
