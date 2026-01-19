import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RendezvousService } from '../../../core/services/rendezvous.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { PatientService } from '../../../core/services/patient.service';
import { RendezVous, StatutRendezVous } from '../../../core/models/rendezvous.model';
import { forkJoin, of, throwError } from 'rxjs';
import { switchMap, catchError, tap, finalize } from 'rxjs/operators';
import { CabinetService } from '../../doctor/services/cabinet.service';
import { NotificationService, DossierPatient } from '../../../core/services/notification.service';

@Component({
    selector: 'app-secretary-orders',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './orders.html',
    styleUrls: ['./orders.css']
})
export class SecretaryOrdersComponent implements OnInit {
    waitingList: RendezVous[] = [];
    confirmedAppointments: RendezVous[] = [];
    currentConsultation?: RendezVous;

    idCabinet!: number;
    selectedMedecinId!: number;

    isLoading: boolean = false;
    errorMessage: string = '';
    dataLoaded: boolean = false;
    isProcessing: boolean = false;

    constructor(
        private rendezvousService: RendezvousService,
        private utilisateurService: UtilisateurService,
        private patientService: PatientService,
        private cabinetService: CabinetService,
        private notificationService: NotificationService,
        private cdRef: ChangeDetectorRef
    ) { }

    ngOnInit() {
        console.log('🔨 SecretaryOrdersComponent initialized');
        this.loadData();
    }


loadData() {
    console.log('🚀 loadData() called');
    this.isLoading = true;
    this.errorMessage = '';
    this.dataLoaded = false;

    // First get current user
    this.utilisateurService.getCurrentUser().pipe(
        tap(user => {
            console.log('👤 Current user:', user);
            
            if (!user?.idUtilisateur) {
                throw new Error('No user ID found');
            }
            
            // Store user ID for later use
            this.selectedMedecinId = user.idUtilisateur;
        }),
        switchMap(user => {
            // Use getCabinetByMedecinId() with current user ID
            return this.cabinetService.getCabinetByMedecinId(user.idUtilisateur);
        }),
        tap(cabinet => {
            console.log('📦 Cabinet loaded:', cabinet);
            
            if (!cabinet?.id) {
                throw new Error('No cabinet found');
            }
            
            this.idCabinet = cabinet.id;
            console.log('✅ Cabinet ID set:', this.idCabinet);
        }),
        switchMap(() => {
            // Get all users in the cabinet to find medecins
            return this.utilisateurService.getUtilisateursByCabinet(this.idCabinet);
        }),
        switchMap(cabinetUsers => {
            console.log('👨‍⚕️ Cabinet users loaded:', cabinetUsers);

            // Find MEDECIN users in the cabinet
            const medecins = cabinetUsers.filter((u: any) => 
                u.role?.toUpperCase() === 'MEDECIN'
            );

            if (medecins.length === 0) {
                // If no medecins found, use the current user (might be a secretary)
                console.warn('⚠️ No medecins found in cabinet, using current user');
                // Keep the current user ID as selectedMedecinId
            } else {
                // Use the first medecin found
                this.selectedMedecinId = medecins[0].idUtilisateur;
                console.log('✅ Selected medecin ID:', this.selectedMedecinId);
            }

            return forkJoin({
                patients: this.patientService.getPatientsByCabinet(this.idCabinet),
                daily: this.rendezvousService.getRendezVousDuJour(this.selectedMedecinId!),
                waiting: this.rendezvousService.getListeAttente(this.selectedMedecinId!)
            });
        }),
        catchError(err => {
            console.error('❌ Error:', err);
            this.errorMessage = err.message || 'Error loading data';
            return of({ patients: [], daily: [], waiting: [] });
        }),
        finalize(() => {
            this.isLoading = false;
            this.cdRef.detectChanges();
            console.log('🔄 Change detection triggered');
        })
    ).subscribe({
        next: ({ patients, daily, waiting }) => {
            console.log('✅ Data received:');
            console.log('- Patients:', patients.length);
            console.log('- Daily appointments:', daily.length);
            console.log('- Waiting list:', waiting.length);

            const mapPatientName = (rdv: RendezVous) => {
                const patient = patients.find(p => p.id === rdv.idPatient);
                return patient ? `${patient.nom} ${patient.prenom}` : 'Unknown Patient';
            };

            this.confirmedAppointments = daily
                .filter(rdv => rdv.statut === StatutRendezVous.CONFIRME)
                .map(rdv => ({
                    ...rdv,
                    patientName: mapPatientName(rdv)
                }))
                .sort((a, b) => {
                    // Sort by time
                    return (a.heureRdv || '').localeCompare(b.heureRdv || '');
                });

            console.log('Confirmed appointments:', this.confirmedAppointments);

            // WAITING LIST: Only PRESENT status
            this.waitingList = waiting
                .filter(rdv => rdv.statut === StatutRendezVous.PRESENT)
                .map(rdv => ({
                    ...rdv,
                    patientName: mapPatientName(rdv)
                }))
                .sort((a, b) => (a.ordrePassage || 0) - (b.ordrePassage || 0));

            console.log('Waiting list:', this.waitingList);

            // CURRENT CONSULTATION: EN_CONSULTATION status
            const inConsultation = daily.find(rdv => rdv.statut === StatutRendezVous.EN_CONSULTATION);
            if (inConsultation) {
                this.currentConsultation = {
                    ...inConsultation,
                    patientName: mapPatientName(inConsultation)
                };
                console.log('Current consultation:', this.currentConsultation);
            } else {
                this.currentConsultation = undefined;
            }

            this.dataLoaded = true;
        },
        error: (err) => {
            console.error('❌ Subscription error:', err);
            this.errorMessage = 'Failed to load data';
        }
    });
}
    /**
     * Mark a confirmed appointment as arrived (PRESENT) and add to waiting list
     */
    markAsArrived(rdv: RendezVous) {
        if (!rdv.id || this.isProcessing) return;

        this.isProcessing = true;
        console.log('✅ Marking patient as arrived:', rdv.id);

        this.rendezvousService.changeStatut(rdv.id, StatutRendezVous.PRESENT).subscribe({
            next: () => {
                console.log('✅ Status changed to PRESENT, adding to waiting list...');

                this.rendezvousService.ajouterEnListeAttente(rdv.id!).subscribe({
                    next: () => {
                        console.log('✅ Patient added to waiting list');
                        this.isProcessing = false;
                        this.loadData();
                    },
                    error: (err) => {
                        console.error('❌ Error adding to waiting list:', err);
                        this.errorMessage = 'Error adding patient to waiting list';
                        this.isProcessing = false;
                    }
                });
            },
            error: (err) => {
                console.error('❌ Error marking as arrived:', err);
                this.errorMessage = 'Error marking patient as arrived';
                this.isProcessing = false;
            }
        });
    }

    /**
     * Call next patient from waiting list OR complete current consultation
     */
    callNextPatient() {
        if (this.isProcessing) return;

        if (this.currentConsultation) {
            // Complete current consultation first
            this.completeCurrentConsultation();
        } else if (this.waitingList.length > 0) {
            // Call next patient from waiting list
            this.startNextConsultation();
        }
    }

    /**
     * Complete the current consultation and call next patient
     */
    private completeCurrentConsultation() {
        if (!this.currentConsultation?.id) return;

        this.isProcessing = true;
        console.log('✅ Completing consultation for:', this.currentConsultation.id);

        this.rendezvousService.changeStatut(this.currentConsultation.id, StatutRendezVous.TERMINE).subscribe({
            next: () => {
                console.log('✅ Consultation completed');
                this.currentConsultation = undefined;
                this.isProcessing = false;

                // Automatically call next patient if available
                if (this.waitingList.length > 0) {
                    setTimeout(() => this.startNextConsultation(), 300);
                } else {
                    this.loadData();
                }
            },
            error: (err) => {
                console.error('❌ Error completing consultation:', err);
                this.errorMessage = 'Error completing consultation';
                this.isProcessing = false;
            }
        });
    }

    /**
     * Start consultation for the next patient in waiting list
     */
    private startNextConsultation() {
    if (this.waitingList.length === 0 || this.isProcessing) return;

    const nextPatient = this.waitingList[0];
    if (!nextPatient?.id || !nextPatient?.idPatient) {
        console.error('❌ Invalid patient data in waiting list:', nextPatient);
        return;
    }

    this.isProcessing = true;
    console.log('✅ Starting consultation for rendez-vous ID:', nextPatient.id);
    console.log('✅ Patient ID from rendez-vous:', nextPatient.idPatient);

    this.patientService.getPatientById(nextPatient.idPatient).pipe(
        switchMap(patient => {
            console.log('✅ Patient retrieved from service:', patient);
            
            if (!patient || !patient.id) {
                console.error('❌ Invalid patient data returned:', patient);
                this.errorMessage = 'Patient information not found';
                this.isProcessing = false;
                return throwError(() => new Error('Patient not found'));
            }

            // Use the actual patient ID from the patient object, not from rendez-vous
            const actualPatientId = patient.id;
            console.log('✅ Actual patient ID to use:', actualPatientId);

            return this.patientService.getPatientDossier(actualPatientId).pipe(
                switchMap(dossier => {
                    // Get cabinet ID
                    const cabinetId = dossier?.idCabinet || this.idCabinet || 0;

                    const dossierPatient = {
                        idPatient: actualPatientId,
                        patientId: actualPatientId,
                        nom: patient.nom,
                        prenom: patient.prenom,
                        nomComplet: `${patient.nom} ${patient.prenom}`,
                        email: patient.email || '',
                        telephone: patient.telephone || '',
                        idDossier: dossier?.idDossier || 0,
                        antecedentsMedicaux: dossier?.antecedentsMedicaux || '',
                        antecedentsChirurgicaux: dossier?.antecedentsChirurgicaux || '',
                        allergies: dossier?.allergies || '',
                        groupeSanguin: dossier?.groupeSanguin || '',
                        remarques: dossier?.remarques || '',
                        dateCreation: dossier?.dateCreation || new Date().toISOString()
                    };

                    console.log('📦 DossierPatient object to send:', dossierPatient);
                    console.log('📤 Will send notification for patient ID:', actualPatientId);

                    return this.notificationService.sendPatientSuivantNotification(
                        this.selectedMedecinId!,
                        dossierPatient,
                        nextPatient.id!,
                        actualPatientId,
                        cabinetId
                    ).pipe(
                        switchMap(() => {
                            console.log('✅ Notification sent, updating status...');
                            return this.rendezvousService.changeStatut(
                                nextPatient.id!,
                                StatutRendezVous.EN_CONSULTATION
                            );
                        }),
                        catchError(notificationError => {
                            console.error('❌ Notification error:', notificationError);
                            console.log('⚠️ Continuing with consultation despite notification error');
                            return this.rendezvousService.changeStatut(
                                nextPatient.id!,
                                StatutRendezVous.EN_CONSULTATION
                            );
                        })
                    );
                }),
                catchError(dossierError => {
                    console.warn('⚠️ Could not load dossier:', dossierError);

                    const minimalDossierPatient = {
                        idPatient: actualPatientId,
                        patientId: actualPatientId,
                        nom: patient.nom,
                        prenom: patient.prenom,
                        nomComplet: `${patient.nom} ${patient.prenom}`,
                        email: patient.email || '',
                        telephone: patient.telephone || '',
                        idDossier: 0,
                        antecedentsMedicaux: '',
                        antecedentsChirurgicaux: '',
                        allergies: '',
                        groupeSanguin: '',
                        remarques: '',
                        dateCreation: new Date().toISOString()
                    };

                    console.log('📦 Minimal DossierPatient (no dossier):', minimalDossierPatient);

                    return this.notificationService.sendPatientSuivantNotification(
                        this.selectedMedecinId!,
                        minimalDossierPatient,
                        nextPatient.id!,
                        actualPatientId,
                        this.idCabinet || 0
                    ).pipe(
                        switchMap(() => {
                            console.log('✅ Minimal notification sent, updating status...');
                            return this.rendezvousService.changeStatut(
                                nextPatient.id!,
                                StatutRendezVous.EN_CONSULTATION
                            );
                        }),
                        catchError(notificationError => {
                            console.error('❌ Notification error with minimal data:', notificationError);
                            return this.rendezvousService.changeStatut(
                                nextPatient.id!,
                                StatutRendezVous.EN_CONSULTATION
                            );
                        })
                    );
                })
            );
        }),
        catchError(err => {
            console.error('❌ Error loading patient:', err);
            this.errorMessage = 'Error loading patient information';
            this.isProcessing = false;
            return throwError(() => err);
        })
    ).subscribe({
        next: () => {
            console.log('✅ Consultation started successfully');
            this.isProcessing = false;
            this.loadData();
        },
        error: (err) => {
            console.error('❌ Error starting consultation:', err);
            this.errorMessage = 'Error starting consultation';
            this.isProcessing = false;
        }
    });
}
    /**
     * Get the button text based on current state
     */
    get callButtonText(): string {
        if (this.currentConsultation) {
            return 'Complete & Call Next';
        }
        return 'Call Next Patient';
    }

    /**
     * Check if call button should be disabled
     */
    get isCallButtonDisabled(): boolean {
        return this.isProcessing || (!this.currentConsultation && this.waitingList.length === 0);
    }

    formatTime(timeString: string | undefined): string {
        if (!timeString) return '';

        if (timeString.length === 5) return timeString;
        if (timeString.length === 8) return timeString.substring(0, 5);

        try {
            const date = new Date(timeString);
            if (!isNaN(date.getTime())) {
                return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
            }
        } catch {
        }

        return timeString;
    }
}