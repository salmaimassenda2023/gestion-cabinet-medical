import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormArray, FormsModule, Validators } from '@angular/forms';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { ConsultationService } from '../../../core/services/consultation.service';
import { PatientService } from '../../../core/services/patient.service';
import { MedicamentService, Medicament } from '../../../core/services/medicament.service';
import { RendezvousService } from '../../../core/services/rendezvous.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { NotificationService, DossierPatient } from '../../../core/services/notification.service';
import { CabinetService, ServiceConsultationDTO } from '../../doctor/services/cabinet.service';
import { debounceTime, distinctUntilChanged, switchMap, of, catchError, map, forkJoin } from 'rxjs';
import { StatutRendezVous } from '../../../core/models/rendezvous.model';
import { ChangeDetectorRef } from '@angular/core';

interface SelectedService extends ServiceConsultationDTO {
    quantite: number;
    sousTotal: number;
}

@Component({
    selector: 'app-consultation',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, FormsModule],
    templateUrl: './consultation.html',
    styleUrls: ['./consultation.css']
})
export class ConsultationComponent implements OnInit {
    consultationForm: FormGroup;
    patientInfo: any = {
        id: 0,
        name: '',
        gender: '',
        age: 0,
        bloodType: 'N/A',
        allergy: 'None',
        currentTreatment: 'N/A',
        lifestyle: 'N/A',
        antecedentsMedicaux: '',
        antecedentsChirurgicaux: ''
    };
    patientDossier: DossierPatient | null = null;
    medicalHistory: any[] = [];
    documents: any[] = [];
    currentRendezVousId?: number;
    currentCabinetId!: number;
    medecinId!: number;

    availableServices: ServiceConsultationDTO[] = [];
    selectedServices: SelectedService[] = [];

    medicationsAutocomplete: Medicament[][] = [];
    showAutocomplete: boolean[] = [];

    isLoading: boolean = false;
    isSaving: boolean = false;

    constructor(
        private fb: FormBuilder,
        private consultationService: ConsultationService,
        private patientService: PatientService,
        private medicamentService: MedicamentService,
        private rendezvousService: RendezvousService,
        private utilisateurService: UtilisateurService,
        private notificationService: NotificationService,
        private cabinetService: CabinetService,
        private router: Router,
        private route: ActivatedRoute,
        private cdr: ChangeDetectorRef
    ) {
        this.consultationForm = this.fb.group({
            temperature: ['', [Validators.pattern(/^\d{1,3}(\.\d{1,2})?$/)]],
            bloodPressure: ['', [Validators.pattern(/^\d{2,3}\/\d{2,3}$/)]],
            heartRate: ['', [Validators.pattern(/^\d{2,3}$/)]],
            observation: [''],
            additionalExams: this.fb.group({
                bloodTests: [false],
                urineTests: [false],
                ecg: [false],
                xray: [false],
                mri: [false],
                ctScan: [false],
                ultrasound: [false]
            }),
            prescription: this.fb.array([]),
            diagnostic: ['', [Validators.required, Validators.minLength(3)]],
            followUpType: ['Months'],
            followUpValue: [''],
            needsFollowUp: [false]
        });
    }

    get prescription() {
        return this.consultationForm.get('prescription') as FormArray;
    }

   ngOnInit() {
    console.log('🔄 ConsultationComponent initialized');
    this.isLoading = true;

    const navigation = this.router.getCurrentNavigation();
    const state = navigation?.extras?.state || history.state;
    
    console.log('🔍 Navigation state:', state);

    this.utilisateurService.getCurrentUser().pipe(
        switchMap(user => {
            console.log('👤 Current user:', user);
            
            if (!user?.idUtilisateur) {
                throw new Error('No user ID found');
            }
            
            this.medecinId = user.idUtilisateur;
            console.log('✅ Medecin ID:', this.medecinId);
            
            return this.cabinetService.getCabinetByMedecinId(this.medecinId);
        }),
        switchMap(cabinet => {
            console.log('🏥 Cabinet loaded:', cabinet);
            
            if (!cabinet?.id) {
                throw new Error('No cabinet found for this medecin');
            }
            
            this.currentCabinetId = cabinet.id;
            console.log('✅ Cabinet ID:', this.currentCabinetId);
            
            return forkJoin({
                services: this.cabinetService.getServices(this.currentCabinetId),
                params: of(this.route.snapshot.queryParams),
                state: of(state)
            });
        }),
        switchMap(({ services, params, state }) => {
            console.log('📋 Services loaded:', services);
            console.log('🔍 Query params:', params);
            console.log('🔍 State received:', state);
            
            this.availableServices = services;
            
            // Auto-select first service if available
            if (services.length > 0) {
                this.addService(services[0]);
                console.log('✅ Auto-selected service:', services[0].nom);
            }
            
            // Priority 1: Check navigation state for patient from notification
            if (state?.patientId) {
                const patientId = Number(state.patientId);
                const rendezVousId = state.rendezVousId ? Number(state.rendezVousId) : undefined;
                
                console.log('🔔 Patient from notification state:', patientId);
                this.currentRendezVousId = rendezVousId;
                return this.loadPatientFromId(patientId);
            }
            
            // Priority 2: Check query params
            if (params['patientId']) {
                const patientId = Number(params['patientId']);
                const rendezVousId = params['rendezVousId'] ? Number(params['rendezVousId']) : undefined;
                
                if (!patientId || isNaN(patientId)) {
                    throw new Error('Invalid patient ID');
                }
                
                console.log('🔗 Patient from query params:', patientId);
                this.currentRendezVousId = rendezVousId;
                return this.loadPatientFromId(patientId);
            }
            
            // Priority 3: Load next patient from waiting list
            console.log('🔍 No patient specified, loading next patient');
            return this.loadNextPatient();
        }),
        catchError(err => {
            console.error('❌ Error in initialization:', err);
            this.isLoading = false;
            this.cdr.detectChanges();
            return of(null);
        })
    ).subscribe({
        next: (result) => {
            console.log('✅ Initialization completed:', result);
            this.isLoading = false;
            this.cdr.detectChanges();
        },
        error: (err) => {
            console.error('❌ Fatal error in initialization:', err);
            this.isLoading = false;
            this.cdr.detectChanges();
        }
    });
}
    // Service Management Methods
    addService(service: ServiceConsultationDTO) {
        const existing = this.selectedServices.find(s => s.idService === service.idService);
        
        if (existing) {
            existing.quantite++;
            existing.sousTotal = existing.prix * existing.quantite;
        } else {
            this.selectedServices.push({
                ...service,
                quantite: 1,
                sousTotal: service.prix
            });
        }
        
        console.log('✅ Service added:', service.nom);
        console.log('📊 Selected services:', this.selectedServices);
    }

    removeService(index: number) {
        const service = this.selectedServices[index];
        console.log('🗑️ Removing service:', service.nom);
        this.selectedServices.splice(index, 1);
    }

    updateServiceQuantity(index: number, quantity: number) {
        if (quantity < 1) return;
        
        const service = this.selectedServices[index];
        service.quantite = quantity;
        service.sousTotal = service.prix * quantity;
        
        console.log('🔄 Updated service quantity:', service.nom, '=', quantity);
    }

    get totalAmount(): number {
        return this.selectedServices.reduce((sum, service) => sum + service.sousTotal, 0);
    }

    get hasServices(): boolean {
        return this.selectedServices.length > 0;
    }

    private loadPatientFromId(patientId: number) {
        console.log('👤 Loading patient with ID:', patientId);
        
        return forkJoin({
            patient: this.patientService.getPatientById(patientId),
            dossier: this.patientService.getPatientDossier(patientId),
            history: this.consultationService.getConsultationsByPatient(patientId).pipe(
                catchError(err => {
                    console.error('Error loading medical history:', err);
                    return of([]);
                })
            ),
            documents: this.patientService.getPatientDocuments(patientId).pipe(
                catchError(err => {
                    console.error('Error loading documents:', err);
                    return of([]);
                })
            )
        }).pipe(
            map(({ patient, dossier, history, documents }) => {
                console.log('✅ All patient data loaded for patient:', patientId);

                this.patientInfo = {
                    id: patient.id,
                    name: `${patient.nom} ${patient.prenom}`,
                    gender: patient.sexe === 'M' ? 'Male' : (patient.sexe === 'F' ? 'Female' : 'N/A'),
                    age: this.calculateAge(patient.dateNaissance),
                    bloodType: dossier.groupeSanguin || 'N/A',
                    allergy: dossier.allergies || 'None',
                    currentTreatment: dossier.traitementsEnCours || 'N/A',
                    lifestyle: dossier.habitudesDeVie || 'N/A',
                    antecedentsMedicaux: dossier.antecedentsMedicaux || '',
                    antecedentsChirurgicaux: dossier.antecedentsChirurgicaux || ''
                };

                this.patientDossier = dossier;
                this.medicalHistory = history;
                this.documents = documents;

                return patient;
            }),
            catchError(err => {
                console.error('❌ Error loading patient data:', err);
                throw err;
            })
        );
    }

    private loadNextPatient() {
        console.log('🔍 Loading next patient for medecin:', this.medecinId);
        
        return this.notificationService.getNextPatientNotification(this.medecinId).pipe(
            switchMap(notification => {
                if (notification?.dossierPatient?.idPatient) {
                    console.log('✅ Found patient in notification:', notification.dossierPatient.idPatient);
                    this.currentRendezVousId = notification.rendezVousId;
                    return this.loadPatientFromId(notification.dossierPatient.idPatient);
                } else {
                    console.log('ℹ️ No notification with patient ID, checking rendezvous...');
                    return this.fetchNextPatientFromRendezvous();
                }
            }),
            catchError(err => {
                console.error('Error loading next patient notification:', err);
                return this.fetchNextPatientFromRendezvous();
            })
        );
    }

    private fetchNextPatientFromRendezvous() {
        console.log('🔍 Checking rendezvous for next patient...');

        return this.rendezvousService.getRendezVousDuJour(this.medecinId).pipe(
            map(rdvs => {
                const activeConsultation = rdvs.find(r => r.statut === StatutRendezVous.EN_CONSULTATION);
                return activeConsultation || null;
            }),
            switchMap(activePatient => {
                if (activePatient) {
                    console.log('✅ Found active consultation:', activePatient);
                    this.currentRendezVousId = activePatient.id;
                    return this.loadPatientFromId(activePatient.idPatient);
                }

                console.log('ℹ️ No active consultation, checking waiting list...');
                return this.rendezvousService.getPatientSuivant(this.medecinId).pipe(
                    switchMap(patient => {
                        if (patient) {
                            console.log('✅ Next patient found via getPatientSuivant:', patient);
                            this.currentRendezVousId = patient.id;
                            return this.loadPatientFromId(patient.idPatient);
                        }

                        console.log('ℹ️ getPatientSuivant returned null, trying full waiting list...');
                        return this.rendezvousService.getListeAttente(this.medecinId).pipe(
                            map(waitingList => {
                                const presentPatients = waitingList.filter(rdv =>
                                    rdv.statut === StatutRendezVous.PRESENT
                                );

                                if (presentPatients.length > 0) {
                                    const sortedPatients = presentPatients.sort((a, b) =>
                                        (a.ordrePassage || 0) - (b.ordrePassage || 0)
                                    );
                                    return sortedPatients[0];
                                }
                                return null;
                            }),
                            switchMap(nextPatient => {
                                if (nextPatient) {
                                    console.log('✅ Next patient from waiting list:', nextPatient);
                                    this.currentRendezVousId = nextPatient.id;
                                    return this.loadPatientFromId(nextPatient.idPatient);
                                }
                                console.log('ℹ️ No patient found via any method');
                                return of(null);
                            })
                        );
                    })
                );
            }),
            catchError(error => {
                console.error('❌ Error finding patient:', error);
                return of(null);
            })
        );
    }

    onDone() {
    if (!this.patientInfo.id) {
        alert('No patient selected');
        return;
    }

    if (!this.currentCabinetId) {
        alert('Error: Cabinet information not available. Please refresh the page.');
        return;
    }

    if (this.consultationForm.invalid) {
        this.consultationForm.markAllAsTouched();
        alert('Please fill in all required fields correctly');
        return;
    }

    if (this.selectedServices.length === 0) {
        alert('Please select at least one service');
        return;
    }

    if (!confirm('Are you sure you want to complete this consultation?')) {
        return;
    }

    this.isSaving = true;
    const val = this.consultationForm.value;

    const consultationData = {
        idPatient: this.patientInfo.id,
        idCabinet: this.currentCabinetId,
        diagnostic: val.diagnostic,
        observation: val.observation || '',
        dateConsultation: new Date().toISOString(),
        services: this.selectedServices.map(service => ({
            idService: service.idService,
            nomService: service.nom,
            prix: service.prix,
            quantite: service.quantite
        }))
    };

    console.log('📤 Creating consultation with services:', consultationData);

    this.consultationService.createConsultation(consultationData).pipe(
        switchMap(consultation => {
            console.log('✅ Consultation created:', consultation);
            const idC = consultation.idConsultation;
            const promises = [];

            // Clinical exams
            if (val.temperature) {
                promises.push(this.consultationService.addExamenClinique(idC, {
                    typeExamen: 'TEMPERATURE',
                    valeur: val.temperature.toString(),
                    unite: '°C'
                }).toPromise());
            }

            if (val.bloodPressure) {
                promises.push(this.consultationService.addExamenClinique(idC, {
                    typeExamen: 'TENSION',
                    valeur: val.bloodPressure,
                    unite: 'mmHg'
                }).toPromise());
            }

            if (val.heartRate) {
                promises.push(this.consultationService.addExamenClinique(idC, {
                    typeExamen: 'FREQUENCE_CARDIAQUE',
                    valeur: val.heartRate.toString(),
                    unite: 'bpm'
                }).toPromise());
            }

            // Medications
            if (this.prescription.length > 0) {
                const ordonnanceMed = {
                    lignes: val.prescription.map((p: any) => ({
                        idMedicament: p.idMedicament || null,
                        nomMedicament: p.nomMedicament,
                        posologie: p.dosage,
                        duree: p.duree,
                        quantite: 1
                    }))
                };
                promises.push(this.consultationService.createOrdonnanceMedicament(idC, ordonnanceMed).toPromise());
            }

            // Additional exams
            const exams = [];
            if (val.additionalExams.bloodTests) exams.push({ typeExamen: 'BLOOD_TEST', description: 'Bilan sanguin complet' });
            if (val.additionalExams.urineTests) exams.push({ typeExamen: 'URINE_TEST', description: 'Analyse urinaire' });
            if (val.additionalExams.ecg) exams.push({ typeExamen: 'ECG', description: 'Électrocardiogramme' });
            if (val.additionalExams.xray) exams.push({ typeExamen: 'XRAY', description: 'Radiographie' });
            if (val.additionalExams.mri) exams.push({ typeExamen: 'MRI', description: 'IRM' });
            if (val.additionalExams.ctScan) exams.push({ typeExamen: 'CT_SCAN', description: 'Scanner' });
            if (val.additionalExams.ultrasound) exams.push({ typeExamen: 'ULTRASOUND', description: 'Échographie' });

            if (exams.length > 0) {
                promises.push(this.consultationService.createOrdonnanceExamen(idC, { lignes: exams }).toPromise());
            }

            return Promise.all(promises).then(() => consultation);
        }),
        switchMap((consultation: any) => {
            // Create invoice with selected services
            const invoiceData = {
                notes: "Consultation terminée",
                services: this.selectedServices.map(service => ({
                    idService: service.idService,
                    nomService: service.nom,
                    prix: service.prix,
                    quantite: service.quantite
                }))
            };

            console.log('💰 Creating invoice with services:', invoiceData);
            return this.consultationService.createFacture(consultation.idConsultation, invoiceData);
        }),
        switchMap(() => {
            if (this.currentRendezVousId) {
                console.log('✅ Completing rendezvous:', this.currentRendezVousId);
                return this.rendezvousService.changeStatut(this.currentRendezVousId, StatutRendezVous.TERMINE);
            }
            return of(null);
        }),
        switchMap(() => {
            if (this.medecinId) {
                return this.notificationService.markAllAsRead(this.medecinId);
            }
            return of(null);
        }),
        catchError(err => {
            console.error('❌ Error during consultation process:', err);
            console.error('❌ Error details:', err.error);
            this.isSaving = false;
            throw err;
        })
    ).subscribe({
        next: () => {
            console.log('✅ Consultation completed successfully!');
            this.isSaving = false;
            alert(`Consultation completed successfully!\nTotal: ${this.totalAmount} MAD`);
            this.router.navigate(['/doctor']);
        },
        error: (err) => {
            console.error('❌ Final error:', err);
            this.isSaving = false;
        }
    });
}
    viewMedicalDossier() {
        if (this.patientInfo.id) {
            this.router.navigate(['/patient', this.patientInfo.id, 'dossier']);
        }
    }

    printMedicationPrescription() {
        if (this.prescription.length === 0) {
            alert('No medication prescribed');
            return;
        }

        const printContent = this.generatePrescriptionPrintContent();
        this.printContent(printContent, 'Medication Prescription');
    }

    printExamPrescription() {
        const exams = this.consultationForm.get('additionalExams')?.value;
        const hasExams = Object.values(exams).some((val: any) => val === true);

        if (!hasExams) {
            alert('No additional exams selected');
            return;
        }

        const printContent = this.generateExamPrintContent();
        this.printContent(printContent, 'Additional Exams Prescription');
    }

    private generatePrescriptionPrintContent(): string {
        const patientName = this.patientInfo.name;
        const date = new Date().toLocaleDateString();
        const medications = this.prescription.value;

        let content = `
            <div style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="text-align: center;">Ordonnance Médicale</h2>
                <hr>
                <p><strong>Patient:</strong> ${patientName}</p>
                <p><strong>Date:</strong> ${date}</p>
                <p><strong>Cabinet:</strong> ${this.currentCabinetId || 'N/A'}</p>
                <hr>
                <h3>Médicaments Prescrits:</h3>
                <table style="width: 100%; border-collapse: collapse;">
                    <thead>
                        <tr>
                            <th style="border: 1px solid #000; padding: 8px;">Médicament</th>
                            <th style="border: 1px solid #000; padding: 8px;">Posologie</th>
                            <th style="border: 1px solid #000; padding: 8px;">Durée</th>
                        </tr>
                    </thead>
                    <tbody>
        `;

        medications.forEach((med: any) => {
            content += `
                <tr>
                    <td style="border: 1px solid #000; padding: 8px;">${med.nomMedicament}</td>
                    <td style="border: 1px solid #000; padding: 8px;">${med.dosage}</td>
                    <td style="border: 1px solid #000; padding: 8px;">${med.duree}</td>
                </tr>
            `;
        });

        content += `
                    </tbody>
                </table>
                <div style="margin-top: 30px; text-align: right;">
                    <p>Signature:</p>
                    <p>_________________________</p>
                    <p>Dr. [Votre Nom]</p>
                </div>
            </div>
        `;

        return content;
    }

    private generateExamPrintContent(): string {
        const patientName = this.patientInfo.name;
        const date = new Date().toLocaleDateString();
        const exams = this.consultationForm.get('additionalExams')?.value;

        const examList = [];
        if (exams.bloodTests) examList.push('Bilan sanguin complet');
        if (exams.urineTests) examList.push('Analyse urinaire');
        if (exams.ecg) examList.push('Électrocardiogramme (ECG)');
        if (exams.xray) examList.push('Radiographie');
        if (exams.mri) examList.push('IRM');
        if (exams.ctScan) examList.push('Scanner');
        if (exams.ultrasound) examList.push('Échographie');

        let content = `
            <div style="font-family: Arial, sans-serif; padding: 20px;">
                <h2 style="text-align: center;">Ordonnance d'Examens Complémentaires</h2>
                <hr>
                <p><strong>Patient:</strong> ${patientName}</p>
                <p><strong>Date:</strong> ${date}</p>
                <p><strong>Cabinet:</strong> ${this.currentCabinetId || 'N/A'}</p>
                <hr>
                <h3>Examens Prescrits:</h3>
                <ul>
        `;

        examList.forEach(exam => {
            content += `<li style="margin-bottom: 10px; font-size: 16px;">${exam}</li>`;
        });

        content += `
                </ul>
                <div style="margin-top: 30px;">
                    <p><strong>Instructions:</strong></p>
                    <p>Veuillez effectuer ces examens dans les plus brefs délais.</p>
                    <p>Présentez cette ordonnance au laboratoire ou centre d'imagerie.</p>
                </div>
                <div style="margin-top: 30px; text-align: right;">
                    <p>Signature:</p>
                    <p>_________________________</p>
                    <p>Dr. [Votre Nom]</p>
                </div>
            </div>
        `;

        return content;
    }

    private printContent(content: string, title: string) {
        const printWindow = window.open('', '_blank', 'width=800,height=600');
        if (!printWindow) {
            alert('Please allow pop-ups to print');
            return;
        }

        printWindow.document.write(`
            <html>
                <head>
                    <title>${title}</title>
                    <style>
                        @media print {
                            @page { margin: 0; }
                            body { margin: 1.6cm; }
                        }
                    </style>
                </head>
                <body onload="window.print(); window.close();">
                    ${content}
                </body>
            </html>
        `);
        printWindow.document.close();
    }

    hasError(controlName: string, errorName: string): boolean {
        const control = this.consultationForm.get(controlName);
        return control ? control.hasError(errorName) && (control.dirty || control.touched) : false;
    }

    hasPrescriptionError(index: number, controlName: string, errorName: string): boolean {
        const control = this.prescription.at(index).get(controlName);
        return control ? control.hasError(errorName) && (control.dirty || control.touched) : false;
    }

    get isLoadingState(): boolean {
        return this.isLoading || this.isSaving;
    }

    addTreatment() {
        const group = this.fb.group({
            idMedicament: [''],
            nomMedicament: ['', Validators.required],
            dosage: ['', Validators.required],
            duree: ['', Validators.required]
        });

        const index = this.prescription.length;
        this.medicationsAutocomplete[index] = [];
        this.showAutocomplete[index] = false;

        group.get('nomMedicament')?.valueChanges.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            switchMap(term => {
                if (term && term.length >= 2) {
                    return this.medicamentService.searchMedicaments(term).pipe(
                        catchError(err => {
                            console.error('Error searching medicaments:', err);
                            return of([]);
                        })
                    );
                }
                return of([]);
            })
        ).subscribe(results => {
            this.medicationsAutocomplete[index] = results;
            this.showAutocomplete[index] = results.length > 0;
        });

        this.prescription.push(group);
    }

    selectMedicament(index: number, med: Medicament) {
        const group = this.prescription.at(index);
        group.patchValue({
            idMedicament: med.id,
            nomMedicament: med.nom,
            dosage: med.dosage
        });
        this.showAutocomplete[index] = false;
    }

    removeTreatment(index: number) {
        this.prescription.removeAt(index);
        this.medicationsAutocomplete.splice(index, 1);
        this.showAutocomplete.splice(index, 1);
    }

    calculateAge(dateS: string | undefined): number {
        if (!dateS) return 0;
        try {
            const birthDate = new Date(dateS);
            const today = new Date();
            let age = today.getFullYear() - birthDate.getFullYear();
            const monthDiff = today.getMonth() - birthDate.getMonth();
            if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
                age--;
            }
            return age;
        } catch {
            return 0;
        }
    }
}