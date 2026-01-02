import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ModalComponent } from '../../../shared/components/modal/modal.component';
import { PaiementService } from '../../../core/services/paiement.service';
import { PatientService } from '../../../core/services/patient.service';
import { UtilisateurService } from '../../auth/services/utilisateur.service';
import { forkJoin } from 'rxjs';

@Component({
    selector: 'app-secretary-payments',
    standalone: true,
    imports: [CommonModule, FormsModule, ModalComponent],
    templateUrl: './payments.html',
    styleUrls: ['./payments.css']
})
export class SecretaryPaymentsComponent implements OnInit {
    payments: any[] = [];
    availableServices: any[] = [
        { id: 1, nomService: 'Blood Test', prix: 50 },
        { id: 2, nomService: 'X-Ray', prix: 120 },
        { id: 3, nomService: 'Vaccination', prix: 25 },
        { id: 4, nomService: 'MRI', prix: 450 }
    ];

    consultationBasePrice = 30;
    isPayModalOpen = false;
    isBillModalOpen = false;

    selectedPatientId?: number;
    selectedConsultationId?: number;
    selectedServices: any[] = [];
    currentBill?: any;

    patients: any[] = [];
    consultations: any[] = [];

    constructor(
        private paiementService: PaiementService,
        private patientService: PatientService,
        private utilisateurService: UtilisateurService
    ) { }

    ngOnInit() {
        this.loadInitialData();
    }

    loadInitialData() {
        this.utilisateurService.getCurrentUser().subscribe({
            next: (user) => {
                if (user.idCabinet) {
                    this.loadPatientsAndPayments(user.idCabinet);
                }
            }
        });
    }

    loadPatientsAndPayments(idCabinet: number) {
        forkJoin({
            patients: this.patientService.getPatientsByCabinet(idCabinet),
            consultations: this.paiementService.searchConsultations() // Get all for now, filter as needed
        }).subscribe({
            next: ({ patients, consultations }) => {
                this.patients = patients;
                this.consultations = consultations;
                this.mapPayments();
            }
        });
    }

    mapPayments() {
        // Flat list of all factures from all consultations
        const allPayments: any[] = [];
        this.consultations.forEach(c => {
            const patient = this.patients.find(p => p.id === c.idPatient);
            if (c.factures && c.factures.length > 0) {
                c.factures.forEach((f: any) => {
                    allPayments.push({
                        ...f,
                        patientName: patient ? `${patient.nom} ${patient.prenom}` : 'Unknown Patient',
                        consultationId: c.idConsultation
                    });
                });
            }
        });

        // Sort by date descending (newest on top)
        this.payments = allPayments.sort((a, b) =>
            new Date(b.dateFacture).getTime() - new Date(a.dateFacture).getTime()
        );
    }

    openPayModal() {
        this.selectedPatientId = undefined;
        this.selectedConsultationId = undefined;
        this.selectedServices = [];
        this.isPayModalOpen = true;
    }

    onPatientChange() {
        if (this.selectedPatientId) {
            this.selectedConsultationId = undefined;
            // Filter consultations for this patient that don't have a paid facture yet
            // (Simplified: showing all consultations of the patient)
        }
    }

    toggleService(service: any) {
        const index = this.selectedServices.findIndex(s => s.id === service.id);
        if (index > -1) {
            this.selectedServices.splice(index, 1);
        } else {
            this.selectedServices.push(service);
        }
    }

    isServiceSelected(service: any): boolean {
        return this.selectedServices.some(s => s.id === service.id);
    }

    get currentTotal(): number {
        return this.consultationBasePrice + this.selectedServices.reduce((acc, s) => acc + s.prix, 0);
    }

    processPayment() {
        if (!this.selectedConsultationId) return;

        const factureData = {
            idConsultation: this.selectedConsultationId,
            notes: "Générée par le secrétariat",
            services: this.selectedServices.map(s => ({
                idService: s.id,
                nomService: s.nomService,
                prix: s.prix
            }))
        };

        this.paiementService.createFacture(this.selectedConsultationId, factureData).subscribe({
            next: (newFacture) => {
                this.isPayModalOpen = false;
                this.ngOnInit(); // Reload to get updated data and sort
                this.currentBill = newFacture;
                this.isBillModalOpen = true;
            },
            error: (err) => console.error('Error creating facture:', err)
        });
    }

    printBill() {
        if (this.currentBill && this.currentBill.idFacture) {
            this.paiementService.generateFacturePDF(this.currentBill.idFacture).subscribe({
                next: (blob) => {
                    const url = window.URL.createObjectURL(blob);
                    const link = document.createElement('a');
                    link.href = url;
                    link.download = `facture_${this.currentBill.idFacture}.pdf`;
                    link.click();
                    // Or open in new tab and print:
                    // window.open(url, '_blank')?.print();
                },
                error: (err) => console.error('Error generating PDF:', err)
            });
        }
    }
}
