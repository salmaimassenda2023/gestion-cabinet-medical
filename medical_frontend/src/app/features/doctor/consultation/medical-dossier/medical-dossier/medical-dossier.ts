import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { PatientService } from '../../../../../core/services/patient.service';
import { ConsultationService } from '../../../../../core/services/consultation.service';

interface DossierMedical {
  idDossier?: number;
  antecedentsMedicaux?: string;
  antecedentsChirurgicaux?: string;
  allergies?: string;
  groupeSanguin?: string;
  remarques?: string;
  traitementsEnCours?: string;
  habitudesDeVie?: string;
  dateCreation?: string;
}

interface DocumentMedical {
  idDocument: number;
  type: string;
  nom: string;
  url: string;
  tailleOctets: number;
  dateUpload: string;
}

@Component({
  selector: 'app-medical-dossier',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, FormsModule],
  templateUrl: './medical-dossier.html',
  styleUrls: ['./medical-dossier.css']
})
export class MedicalDossierComponent implements OnInit {
  dossierForm: FormGroup;
  documentUploadForm: FormGroup; // Add separate form for document upload

  patientInfo: any = null;
  dossierData: DossierMedical | null = null;
  documents: DocumentMedical[] = [];
  consultationHistory: any[] = [];

  patientId!: number;
  isLoading = false;
  isSaving = false;
  isEditMode = false;
  isUploadingDocument = false;

  selectedFile: File | null = null;

  documentTypes = [
    { value: 'ANALYSIS', label: 'Blood/Lab Analysis', icon: 'fa-flask' },
    { value: 'IMAGING', label: 'Medical Imaging', icon: 'fa-x-ray' },
    { value: 'PRESCRIPTION', label: 'Prescription', icon: 'fa-prescription' },
    { value: 'REPORT', label: 'Medical Report', icon: 'fa-file-medical' },
    { value: 'OTHER', label: 'Other', icon: 'fa-file' }
  ];

  bloodTypes = ['A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private patientService: PatientService,
    private consultationService: ConsultationService,
    private cdr: ChangeDetectorRef
  ) {
    // Main dossier form
    this.dossierForm = this.fb.group({
      antecedentsMedicaux: [''],
      antecedentsChirurgicaux: [''],
      allergies: [''],
      groupeSanguin: [''],
      remarques: [''],
      traitementsEnCours: [''],
      habitudesDeVie: ['']
    });

    this.documentUploadForm = this.fb.group({
      typeDocument: ['ANALYSIS']
    });
  }

  ngOnInit() {
    console.log('🚀 Medical Dossier Component Initialized');

    this.route.params.subscribe(params => {
      this.patientId = +params['id'];
      console.log('📋 Patient ID from route:', this.patientId);

      if (this.patientId && !isNaN(this.patientId)) {
        this.loadPatientData();
      } else {
        console.error('❌ Invalid patient ID:', params['id']);
        alert('Invalid patient ID');
        this.router.navigate(['/doctor']);
      }
    });
  }
  loadPatientData() {
    this.isLoading = true;
    this.cdr.detectChanges();
    console.log('🔄 Loading patient data for ID:', this.patientId);

    Promise.all([
      this.loadPatientInfo(),
      this.loadDossierData(),
      this.loadDocuments(),
      this.loadConsultationHistory()
    ]).then(() => {
      this.isLoading = false;
      this.cdr.detectChanges();
      console.log('✅ All data loaded successfully');
    }).catch((error) => {
      console.error('❌ Error loading patient data:', error);
      this.isLoading = false;
      this.cdr.detectChanges();
      alert('Error loading patient data');
    });
  }

  private loadPatientInfo(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.patientService.getPatientById(this.patientId).subscribe({
        next: (patient) => {
          console.log('✅ Patient loaded:', patient);
          this.patientInfo = patient;
          this.cdr.detectChanges();
          resolve();
        },
        error: (err) => {
          console.error('❌ Error loading patient info:', err);
          reject(err);
        }
      });
    });
  }

  private loadDossierData(): Promise<void> {
    return new Promise((resolve) => {
      this.patientService.getPatientDossier(this.patientId).subscribe({
        next: (dossier) => {
          console.log('✅ Dossier loaded:', dossier);
          this.dossierData = dossier;
          if (dossier) {
            this.dossierForm.patchValue({
              antecedentsMedicaux: dossier.antecedentsMedicaux || '',
              antecedentsChirurgicaux: dossier.antecedentsChirurgicaux || '',
              allergies: dossier.allergies || '',
              groupeSanguin: dossier.groupeSanguin || '',
              remarques: dossier.remarques || '',
              traitementsEnCours: dossier.traitementsEnCours || '',
              habitudesDeVie: dossier.habitudesDeVie || ''
            });
          }
          this.cdr.detectChanges();
          resolve();
        },
        error: (err) => {
          console.warn('⚠️ Could not load dossier:', err);
          this.dossierData = null;
          this.cdr.detectChanges();
          resolve();
        }
      });
    });
  }

  private loadDocuments(): Promise<void> {
    return new Promise((resolve) => {
      this.patientService.getPatientDocuments(this.patientId).subscribe({
        next: (documents) => {
          console.log('✅ Documents loaded:', documents?.length || 0);
          this.documents = documents || [];
          this.cdr.detectChanges();
          resolve();
        },
        error: (err) => {
          console.warn('⚠️ Could not load documents:', err);
          this.documents = [];
          this.cdr.detectChanges();
          resolve();
        }
      });
    });
  }

  private loadConsultationHistory(): Promise<void> {
    return new Promise((resolve) => {
      this.consultationService.getConsultationsByPatient(this.patientId).subscribe({
        next: (history) => {
          console.log('✅ Consultation history loaded:', history?.length || 0);
          this.consultationHistory = history || [];
          this.cdr.detectChanges();
          resolve();
        },
        error: (err) => {
          console.warn('⚠️ Could not load consultation history:', err);
          this.consultationHistory = [];
          this.cdr.detectChanges();
          resolve();
        }
      });
    });
  }

  toggleEditMode() {
    if (this.isEditMode) {
      if (this.dossierData) {
        this.dossierForm.patchValue({
          antecedentsMedicaux: this.dossierData.antecedentsMedicaux || '',
          antecedentsChirurgicaux: this.dossierData.antecedentsChirurgicaux || '',
          allergies: this.dossierData.allergies || '',
          groupeSanguin: this.dossierData.groupeSanguin || '',
          remarques: this.dossierData.remarques || '',
          traitementsEnCours: this.dossierData.traitementsEnCours || '',
          habitudesDeVie: this.dossierData.habitudesDeVie || ''
        });
      }
    }
    this.isEditMode = !this.isEditMode;
    this.cdr.detectChanges();
  }

  saveDossier() {
    if (this.dossierForm.invalid) {
      this.dossierForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.cdr.detectChanges();

    const dossierData = this.dossierForm.value;

    this.patientService.updatePatientDossier(this.patientId, dossierData).subscribe({
      next: () => {
        alert('Medical dossier updated successfully!');
        this.isEditMode = false;
        this.isSaving = false;
        this.cdr.detectChanges();
        this.loadPatientData();
      },
      error: (err) => {
        console.error('Error saving dossier:', err);
        alert('Error saving medical dossier');
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 10 * 1024 * 1024) {
        alert('File size must be less than 10MB');
        return;
      }
      this.selectedFile = file;
      this.cdr.detectChanges();
    }
  }

  uploadDocument() {
    if (!this.selectedFile) {
      alert('Please select a file');
      return;
    }

    this.isUploadingDocument = true;
    this.cdr.detectChanges();

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    const selectedType = this.documentUploadForm.get('typeDocument')?.value || 'ANALYSIS';
    formData.append('type', selectedType);

    this.patientService.uploadDocument(this.patientId, formData).subscribe({
      next: (doc) => {
        this.documents.push(doc);
        this.selectedFile = null;
        this.isUploadingDocument = false;
        this.cdr.detectChanges();
        alert('Document uploaded successfully!');

        const fileInput = document.getElementById('fileInput') as HTMLInputElement;
        if (fileInput) fileInput.value = '';
      },
      error: (err) => {
        console.error('Error uploading document:', err);
        alert('Error uploading document');
        this.isUploadingDocument = false;
        this.cdr.detectChanges();
      }
    });
  }

  deleteDocument(docId: number) {
    if (!confirm('Are you sure you want to delete this document?')) {
      return;
    }

    this.patientService.deleteDocument(docId).subscribe({
      next: () => {
        this.documents = this.documents.filter(d => d.idDocument !== docId);
        this.cdr.detectChanges();
        alert('Document deleted successfully!');
      },
      error: (err) => {
        console.error('Error deleting document:', err);
        alert('Error deleting document');
      }
    });
  }

  downloadDocument(doc: DocumentMedical) {
    window.open(doc.url, '_blank');
  }

  formatFileSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  }

  getDocumentIcon(type: string): string {
    const docType = this.documentTypes.find(t => t.value === type);
    return docType ? docType.icon : 'fa-file';
  }

  calculateAge(dateNaissance: string): number {
    if (!dateNaissance) return 0;
    const birthDate = new Date(dateNaissance);
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDiff = today.getMonth() - birthDate.getMonth();
    if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
      age--;
    }
    return age;
  }
}