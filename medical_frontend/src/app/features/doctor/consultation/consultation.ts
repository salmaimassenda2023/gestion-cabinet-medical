import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, FormArray, FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ConsultationService } from '../../../core/services/consultation.service';
import { PatientService } from '../../../core/services/patient.service';
import { MedicamentService, Medicament } from '../../../core/services/medicament.service';
import { RendezvousService } from '../../../core/services/rendezvous.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { debounceTime, distinctUntilChanged, switchMap, of } from 'rxjs';
import { StatutRendezVous } from '../../../core/models/rendezvous.model';

@Component({
    selector: 'app-consultation',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule, FormsModule],
    templateUrl: './consultation.html',
    styleUrls: ['./consultation.css']
})
export class ConsultationComponent implements OnInit {
    consultationForm: FormGroup;
    patientInfo: any = {};
    medicalHistory: any[] = [];
    nextPatientRendezVous: any;

    medicationsAutocomplete: Medicament[][] = [];
    showAutocomplete: boolean[] = [];

    constructor(
        private fb: FormBuilder,
        private consultationService: ConsultationService,
        private patientService: PatientService,
        private medicamentService: MedicamentService,
        private rendezvousService: RendezvousService,
        private utilisateurService: UtilisateurService,
        private router: Router
    ) {
        this.consultationForm = this.fb.group({
            temperature: [''],
            bloodPressure: [''],
            heartRate: [''],
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
            diagnostic: [''],
            followUpType: ['Months'],
            followUpValue: [''],
            needsFollowUp: [false]
        });
    }

    get prescription() {
        return this.consultationForm.get('prescription') as FormArray;
    }

    ngOnInit() {
        this.loadCurrentMedecinAndNextPatient();
    }

    loadCurrentMedecinAndNextPatient() {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user) => {
                if (user.role === 'MEDECIN' && user.idUtilisateur) {
                    this.fetchNextPatient(user.idUtilisateur);
                }
            }
        });
    }

    fetchNextPatient(medecinId: number) {
        this.rendezvousService.getPatientSuivant(medecinId).subscribe({
            next: (rv) => {
                if (rv) {
                    this.nextPatientRendezVous = rv;
                    this.loadPatientDetails(rv.idPatient);
                    this.loadMedicalHistory(rv.idPatient);
                    // Update status to EN_CONSULTATION automatically when entering
                    if (rv.id !== undefined) {
                        this.rendezvousService.changeStatut(rv.id, StatutRendezVous.EN_CONSULTATION).subscribe();
                    }
                }
            },
            error: (err) => console.error('Error fetching next patient:', err)
        });
    }

    loadPatientDetails(patientId: number) {
        this.patientService.getPatientById(patientId).subscribe({
            next: (patient) => {
                this.patientInfo = {
                    name: `${patient.nom} ${patient.prenom}`,
                    gender: patient.sexe === 'M' ? 'Male' : (patient.sexe === 'F' ? 'Female' : 'N/A'),
                    age: this.calculateAge(patient.dateNaissance),
                    bloodType: 'N/A', // Not in current model
                    allergy: 'None', // Not in current model
                    currentTreatment: 'N/A',
                    lifestyle: 'N/A'
                };
            }
        });
    }

    loadMedicalHistory(patientId: number) {
        this.consultationService.getConsultationsByPatient(patientId).subscribe({
            next: (consultations) => {
                this.medicalHistory = consultations.map(c => ({
                    date: new Date(c.dateConsultation).toLocaleDateString(),
                    type: 'Consultation',
                    diagnostic: c.diagnostic || 'No diagnostic'
                }));
            }
        });
    }

    calculateAge(dateS: string): number {
        if (!dateS) return 0;
        const birthDate = new Date(dateS);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const m = today.getMonth() - birthDate.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    }

    addTreatment() {
        const group = this.fb.group({
            idMedicament: [''],
            nomMedicament: [''],
            dosage: [''],
            duree: ['']
        });

        const index = this.prescription.length;
        this.medicationsAutocomplete[index] = [];
        this.showAutocomplete[index] = false;

        group.get('nomMedicament')?.valueChanges.pipe(
            debounceTime(300),
            distinctUntilChanged(),
            switchMap(term => term && term.length >= 2 ? this.medicamentService.searchMedicaments(term) : of([]))
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

    onDone() {
        if (!this.nextPatientRendezVous) return;

        const val = this.consultationForm.value;
        const consultationData = {
            idPatient: this.nextPatientRendezVous.idPatient,
            idCabinet: this.nextPatientRendezVous.idCabinet,
            diagnostic: val.diagnostic,
            services: [{ idService: 1, nomService: 'Consultation Générale', prix: 200 }] // Example
        };

        this.consultationService.createConsultation(consultationData).subscribe({
            next: (consultation) => {
                const idC = consultation.idConsultation;

                // 1. Clinical Exams
                if (val.temperature) this.consultationService.addExamenClinique(idC, { typeExamen: 'TEMPERATURE', valeur: val.temperature, unite: '°C' }).subscribe();
                if (val.bloodPressure) this.consultationService.addExamenClinique(idC, { typeExamen: 'TENSION', valeur: val.bloodPressure, unite: 'mmHg' }).subscribe();
                if (val.heartRate) this.consultationService.addExamenClinique(idC, { typeExamen: 'FREQUENCE_CARDIAQUE', valeur: val.heartRate, unite: 'bpm' }).subscribe();

                // 2. Prescription Medications
                if (this.prescription.length > 0) {
                    const ordonnanceMed = {
                        lignes: val.prescription.map((p: any) => ({
                            idMedicament: p.idMedicament || 'manual',
                            nomMedicament: p.nomMedicament,
                            posologie: p.dosage,
                            duree: p.duree
                        }))
                    };
                    this.consultationService.createOrdonnanceMedicament(idC, ordonnanceMed).subscribe();
                }

                // 3. Prescription Additional Exams
                const exams = [];
                if (val.additionalExams.bloodTests) exams.push({ typeExamen: 'BLOOD_TEST', description: 'Bilan sanguin complet' });
                if (val.additionalExams.ecg) exams.push({ typeExamen: 'ECG', description: 'Électrocardiogramme' });
                if (val.additionalExams.xray) exams.push({ typeExamen: 'XRAY', description: 'Radiographie' });
                if (val.additionalExams.mri) exams.push({ typeExamen: 'MRI', description: 'IRM' });
                if (val.additionalExams.ultrasound) exams.push({ typeExamen: 'ULTRASOUND', description: 'Échographie' });

                if (exams.length > 0) {
                    this.consultationService.createOrdonnanceExamen(idC, { lignes: exams }).subscribe();
                }

                // 4. Generate Invoice
                const invoiceData = {
                    idConsultation: idC,
                    notes: "Consultation terminée",
                    services: consultationData.services
                };
                this.consultationService.createFacture(idC, invoiceData).subscribe();

                // 5. Terminate Rendezvous
                this.rendezvousService.changeStatut(this.nextPatientRendezVous.id, StatutRendezVous.TERMINE).subscribe({
                    next: () => {
                        this.router.navigate(['/doctor']);
                    }
                });
            },
            error: (err) => console.error('Error creating consultation:', err)
        });
    }
}
