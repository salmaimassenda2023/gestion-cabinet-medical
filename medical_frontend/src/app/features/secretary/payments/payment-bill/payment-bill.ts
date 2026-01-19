import { Component, OnInit, OnChanges, Input, Output, EventEmitter, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaiementService } from '../../../../core/services/paiement.service';
import { ConsultationService } from '../../../../core/services/consultation.service';
import { PatientService } from '../../../../core/services/patient.service';

@Component({
    selector: 'app-payment-form',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './payment-bill.html',
    styleUrls: ['./payment-bill.css']
})
export class PaymentFormComponent implements OnInit, OnChanges {
    @Input() isOpen: boolean = false;
    @Input() cabinetId: number | null = null;
    @Output() close = new EventEmitter<void>();
    @Output() paymentCreated = new EventEmitter<any>();

    // Data arrays
    patients: any[] = [];
    cabinetServices: any[] = [];

    // Selected items
    selectedPatientId?: number;
    selectedPatientConsultations: any[] = [];
    selectedConsultationId?: number;
    selectedServices: any[] = [];

    // Form data
    invoiceNotes: string = '';
    consultationBasePrice: number = 0;

    // UI states
    isLoading: boolean = false;
    isProcessing: boolean = false;

    // Consultation details cache
    consultationDetails: Map<number, any> = new Map();

    constructor(
        private paiementService: PaiementService,
        private consultationService: ConsultationService,
        private patientService: PatientService
    ) { }

    ngOnInit(): void {
        console.log('PaymentFormComponent initialized, isOpen:', this.isOpen);
        if (this.isOpen) {
            this.loadData();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        console.log('PaymentFormComponent changes:', changes);
        if (changes['isOpen']) {
            console.log('isOpen changed to:', changes['isOpen'].currentValue);
            if (changes['isOpen'].currentValue) {
                this.loadData();
            } else if (!changes['isOpen'].currentValue && !changes['isOpen'].firstChange) {
                this.resetForm();
            }
        }
    }

    loadData(): void {
    console.log('🔄 PaymentFormComponent - Loading form data...');
    this.isLoading = true;
    
    // Clear previous data
    this.patients = [];
    this.cabinetServices = [];

    console.log(`📋 Cabinet ID for loading: ${this.cabinetId}`);

    // Load patients first, then services
    this.loadPatients()
        .then(() => {
            console.log(`✅ Patients loaded: ${this.patients.length}`);
            
            return this.loadCabinetServices();
        })
        .then(() => {
            console.log(`✅ Services loaded: ${this.cabinetServices.length}`);
            console.log('🎉 Form data loaded successfully!');
        })
        .catch(error => {
            console.error('❌ Error loading form data:', error);
            if (!error.message?.includes('services')) {
                alert('Error loading form data. Please try again.');
            }
        })
        .finally(() => {
            console.log('🏁 Form loading completed');
            this.isLoading = false;
        });
}

    loadPatients(): Promise<void> {
        return new Promise((resolve, reject) => {
            this.patientService.getAllPatients().subscribe({
                next: (data) => {
                    this.patients = data || [];
                    console.log('Loaded patients:', this.patients.length);
                    resolve();
                },
                error: (error) => {
                    console.error('Error loading patients:', error);
                    this.patients = [];
                    reject(error);
                }
            });
        });
    }

  loadCabinetServices(): Promise<void> {
    return new Promise((resolve) => {
        if (!this.cabinetId) {
            console.warn('⚠️ No cabinet ID provided for services');
            this.cabinetServices = [];
            resolve();
            return;
        }

        console.log(`🩺 Loading services for cabinet ID: ${this.cabinetId}`);
        
        this.paiementService.getCabinetServices(this.cabinetId).subscribe({
            next: (data) => {
                console.log('📦 Services response received:', data);
                
                this.cabinetServices = data || [];
                
                this.cabinetServices = this.cabinetServices.map(service => ({
                    ...service,
                    isIncluded: service.obligatoire === true
                }));
                
                console.log(`✅ ${this.cabinetServices.length} services loaded`);
                
                resolve();
            },
            error: (error) => {
                console.error('❌ Error loading services:', error);
                console.log('ℹ️ Continuing without services');
                this.cabinetServices = [];
                resolve();
            }
        });
    });
}
onPatientChange(): void {
    console.log('Patient changed (raw):', this.selectedPatientId);
    console.log('Type of selectedPatientId:', typeof this.selectedPatientId);
    
    // Convert to number
    let patientId: number | undefined;
    
    if (this.selectedPatientId) {
        // Handle both string and number cases
        if (typeof this.selectedPatientId === 'string') {
            patientId = parseInt(this.selectedPatientId, 10);
        } else {
            patientId = this.selectedPatientId;
        }
        
        console.log('Converted patientId:', patientId);
    }
    
    if (patientId && patientId > 0 && !isNaN(patientId)) {
        console.log(`✅ Loading consultations for patient ID: ${patientId}`);
        this.loadPatientConsultations(patientId);
        this.selectedConsultationId = undefined;
        this.selectedServices = [];
        this.consultationBasePrice = 0;
    } else {
        console.log('❌ No valid patient selected, clearing consultation data');
        this.selectedPatientConsultations = [];
        this.selectedConsultationId = undefined;
        this.selectedServices = [];
        this.consultationBasePrice = 0;
    }
}
loadPatientConsultations(patientId: number): void {
    // Validate patientId
    if (!patientId || patientId <= 0) {
        console.error('Invalid patient ID:', patientId);
        this.selectedPatientConsultations = [];
        return;
    }
    
    console.log('Loading consultations for patient:', patientId);
    this.consultationService.getConsultationsByPatient(patientId).subscribe({
        next: (data) => {
            console.log('📦 Raw consultations data:', data);
            
            if (data && data.length > 0) {
                console.log('First consultation keys:', Object.keys(data[0]));
            }
            
            this.selectedPatientConsultations = (data || []).map((consultation, index) => {
                if (!consultation.idConsultation) {
                    console.warn('⚠️ Consultation missing ID, cannot be used for invoicing');
                    return null;
                }
                
                return {
                    idConsultation: consultation.idConsultation, 
                    dateConsultation: consultation.dateConsultation,
                    motif: consultation.motif || consultation.diagnostic || `Consultation ${index + 1}`,
                    prixConsultation: consultation.prixConsultation || consultation.montantTotal || 300,
                    statut: consultation.statut || 'TERMINE',
                    diagnostic: consultation.diagnostic || 'No diagnosis'
                };
            }).filter(c => c !== null); 
            
            console.log(`✅ Created ${this.selectedPatientConsultations.length} consultation objects`);
        },
        error: (error) => {
            console.error('Error loading consultations:', error);
            this.selectedPatientConsultations = [];
        }
    });
}
   
    getConsultationDetails(consultationId: number): any {
    if (this.consultationDetails.has(consultationId)) {
        return this.consultationDetails.get(consultationId);
    }
    
    const consultation = this.selectedPatientConsultations.find(
        c => c.idConsultation === consultationId
    );
    
    return consultation || {};
}

   
    // Calculate totals
    get servicesTotal(): number {
        return this.selectedServices.reduce((sum, service) => sum + (service.prix || 0), 0);
    }

    get totalAmount(): number {
        return this.consultationBasePrice + this.servicesTotal;
    }

    // Service selection
    toggleService(service: any): void {
        const index = this.selectedServices.findIndex(s => s.idService === service.idService);

        if (index > -1) {
            this.selectedServices.splice(index, 1);
        } else {
            this.selectedServices.push({ ...service });
        }
        console.log('Selected services:', this.selectedServices.length);
    }

    isServiceSelected(service: any): boolean {
        return this.selectedServices.some(s => s.idService === service.idService);
    }

    removeService(index: number): void {
        this.selectedServices.splice(index, 1);
    }

    // Process payment
    processPayment(): void {
    if (!this.selectedConsultationId) {
        alert('Please select a consultation');
        return;
    }

    console.log('💳 Processing payment...');
    console.log(`📋 Consultation: ${this.selectedConsultationId}`);
    console.log(`💰 Base price: ${this.consultationBasePrice} MAD`);
    console.log(`🩺 Selected services: ${this.selectedServices.length}`);
    console.log(`💵 Total: ${this.totalAmount} MAD`);

    this.isProcessing = true;

    const factureData = {
        statut: 'EN_ATTENTE',
        notes: this.invoiceNotes || 'Consultation invoice',
        serviceIds: this.selectedServices.map(s => s.idService), // Can be empty array
        consultationId: this.selectedConsultationId,
        montantTotal: this.totalAmount
    };

    console.log('📄 Invoice data:', factureData);

    this.paiementService.createFacture(this.selectedConsultationId, factureData).subscribe({
        next: (response) => {
            console.log('✅ Invoice created successfully:', response);
            alert('Invoice created successfully!');
            this.paymentCreated.emit(response);
            this.closeForm();
        },
        error: (error) => {
            console.error('❌ Error creating invoice:', error);
            alert(`Error creating invoice: ${error.error?.message || error.message || 'Unknown error'}`);
        },
        complete: () => {
            this.isProcessing = false;
        }
    });
}

    resetForm(): void {
        console.log('Resetting form...');
        this.selectedPatientId = undefined;
        this.selectedPatientConsultations = [];
        this.selectedConsultationId = undefined;
        this.selectedServices = [];
        this.invoiceNotes = '';
        this.consultationBasePrice = 0;
        this.consultationDetails.clear();
    }

    closeForm(): void {
        console.log('Closing form...');
        this.resetForm();
        this.close.emit();
    }

    calculateAge(dateOfBirth: string): number {
        if (!dateOfBirth) return 0;
        const dob = new Date(dateOfBirth);
        const today = new Date();
        let age = today.getFullYear() - dob.getFullYear();
        const monthDiff = today.getMonth() - dob.getMonth();

        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < dob.getDate())) {
            age--;
        }

        return age;
    }

    getSelectedPatientName(): string {
        if (!this.selectedPatientId) return '';
        const patient = this.patients.find(p => p.idPatient === this.selectedPatientId);
        return patient ? `${patient.prenom} ${patient.nom}` : '';
    }

    formatDate(dateString: string): string {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
    debugPatientChange(event: any): void {
    console.log('=== DEBUG PATIENT CHANGE ===');
    console.log('Event:', event);
    console.log('Event target:', event.target);
    console.log('Event target value:', event.target.value);
    console.log('Event target selectedIndex:', event.target.selectedIndex);
    console.log('Selected option:', event.target.options[event.target.selectedIndex]);
    console.log('Patients array:', this.patients);
}
getConsultationDisplayText(consultation: any, index: number): string {
    const date = this.formatDate(consultation.dateConsultation);
    const type = consultation.motif || `Consultation ${index + 1}`;
    const price = consultation.prixConsultation || 300;
    
    return `${date} - ${type} (${price} MAD)`;
}

getSelectedConsultation(): any {
    if (!this.selectedConsultationId) return null;
    return this.selectedPatientConsultations.find(
        c => c.idConsultation === this.selectedConsultationId
    );
}

onConsultationChange(): void {
    console.log('Consultation changed:', this.selectedConsultationId);
    
    if (this.selectedConsultationId) {
        const consultation = this.getSelectedConsultation();
        this.consultationBasePrice = consultation?.prixConsultation || 300;
        console.log('Consultation base price:', this.consultationBasePrice);
    } else {
        this.consultationBasePrice = 0;
    }
}
}