import { Component, ElementRef, ViewChild, AfterViewInit, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { CabinetService, CabinetResponse, ServiceConsultationDTO } from '../../../doctor/services/cabinet.service';
import { UtilisateurService, UtilisateurRequest, UtilisateurResponse } from '../../../auth/services/utilisateur.service';
import { ActivatedRoute } from '@angular/router';
import { NgxStripeModule, StripeCardComponent, StripeService } from 'ngx-stripe';
import { StripeCardElementOptions, StripeElementsOptions } from '@stripe/stripe-js';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-doctor-onboarding',
  standalone: true,
  templateUrl: './doctor-onboarding.html',
  styleUrls: ['./doctor-onboarding.css'],
  imports: [CommonModule, ReactiveFormsModule, NgxStripeModule]
})
export class DoctorOnboardingComponent implements AfterViewInit, OnInit {
  currentStep = 1;
  totalSteps = 4;
  isLoading = false;
  errorMessage: string | null = null;
  selectedPlan: string = 'monthly';

  // Stripe
  @ViewChild(StripeCardComponent) card!: StripeCardComponent;
  cardOptions: StripeCardElementOptions = {
    style: {
      base: {
        iconColor: '#666EE8',
        color: '#31325F',
        lineHeight: '40px',
        fontWeight: '300',
        fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
        fontSize: '18px',
        '::placeholder': {
          color: '#CFD7E0'
        }
      }
    }
  };
  elementsOptions: StripeElementsOptions = {
    locale: 'en'
  };

  // Step 1: Personal Info
  personalForm: FormGroup;
  signatureMode: 'upload' | 'draw' = 'draw';
  @ViewChild('signatureCanvas') signatureCanvas!: ElementRef<HTMLCanvasElement>;
  private canvasContext: CanvasRenderingContext2D | null = null;
  private isDrawing = false;
  signatureImage: string | null = null;

  // Step 2: Clinic Info
  clinicForm: FormGroup;
  logoPreview: string | null = null;

  // Step 3: Services & Pricing
  servicesForm: FormGroup;

  // Step 4: Payment
  paymentForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private cabinetService: CabinetService,
    private utilisateurService: UtilisateurService,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private stripeService: StripeService,
    private http: HttpClient
  ) {
    this.personalForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(8)]],
      tel: ['', Validators.required],
      signature: [null]
    });

    this.clinicForm = this.fb.group({
      name: ['', Validators.required],
      specialty: ['', Validators.required],
      address: ['', Validators.required],
      tel: ['', Validators.required],
      maxPatients: ['', [Validators.required, Validators.min(1)]],
      consultationTime: ['', [Validators.required, Validators.min(5)]],
      logo: [null]
    });

    this.servicesForm = this.fb.group({
      generalPrice: ['', Validators.required],
      additionalServices: this.fb.array([])
    });

    this.paymentForm = this.fb.group({
      cardName: ['', Validators.required]
    });
  }

  get additionalServices() {
    return this.servicesForm.get('additionalServices') as FormArray;
  }

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['plan']) {
        this.selectedPlan = params['plan'];
      }
    });
  }

  ngAfterViewInit() {
    if (this.signatureMode === 'draw') {
      setTimeout(() => this.initCanvas(), 0);
    }
  }

  // Navigation
  public nextStep() {
    if (this.currentStep < this.totalSteps) {
      if (this.validateCurrentStep()) {
        this.currentStep++;
      }
    } else {
      this.submit();
    }
  }

  public prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  public validateCurrentStep(): boolean {
    switch (this.currentStep) {
      case 1:
        if (this.personalForm.invalid) {
          this.personalForm.markAllAsTouched();
          return false;
        }
        if (!this.signatureImage && !this.personalForm.get('signature')?.value) {
          alert("Please provide a signature.");
          return false;
        }
        return true;
      case 2:
        if (this.clinicForm.invalid) {
          this.clinicForm.markAllAsTouched();
          return false;
        }
        return true;
      case 3:
        if (this.servicesForm.invalid) {
          this.servicesForm.markAllAsTouched();
          return false;
        }
        return true;
      case 4:
        return true;
      default: return false;
    }
  }

  // Signature Logic
  public setSignatureMode(mode: 'upload' | 'draw') {
    this.signatureMode = mode;
    if (mode === 'draw') {
      setTimeout(() => this.initCanvas(), 50);
    }
  }

  private initCanvas() {
    if (!this.signatureCanvas) return;
    const canvas = this.signatureCanvas.nativeElement;
    this.canvasContext = canvas.getContext('2d');
    if (this.canvasContext) {
      this.canvasContext.lineWidth = 2;
      this.canvasContext.lineCap = 'round';
      this.canvasContext.strokeStyle = '#000';
    }
  }

  public startDrawing(e: MouseEvent | TouchEvent) {
    this.isDrawing = true;
    const pos = this.getEventPos(e);
    this.canvasContext?.beginPath();
    this.canvasContext?.moveTo(pos.x, pos.y);
  }

  public draw(e: MouseEvent | TouchEvent) {
    if (!this.isDrawing) return;
    e.preventDefault();
    const pos = this.getEventPos(e);
    this.canvasContext?.lineTo(pos.x, pos.y);
    this.canvasContext?.stroke();
  }

  public stopDrawing() {
    if (!this.isDrawing) return;
    this.isDrawing = false;
    this.saveSignature();
  }

  private getEventPos(e: MouseEvent | TouchEvent) {
    const canvas = this.signatureCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;
    return {
      x: clientX - rect.left,
      y: clientY - rect.top
    };
  }

  public clearSignature() {
    const canvas = this.signatureCanvas.nativeElement;
    this.canvasContext?.clearRect(0, 0, canvas.width, canvas.height);
    this.signatureImage = null;
  }

  private saveSignature() {
    this.signatureImage = this.signatureCanvas.nativeElement.toDataURL();
    this.personalForm.patchValue({ signature: this.signatureImage });
    this.cd.detectChanges();
  }

  public onFileSelected(event: Event, field: 'signature' | 'logo') {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      const reader = new FileReader(); reader.onload = () => {
        if (field === 'signature') {
          this.personalForm.patchValue({ signature: reader.result });
          this.signatureImage = reader.result as string;
          this.cd.detectChanges();
        } else {
          this.clinicForm.patchValue({ logo: reader.result });
          this.logoPreview = reader.result as string;
          this.cd.detectChanges();
        }
      };
      reader.readAsDataURL(file);
    }
  }

  // Services Logic
  public addService() {
    const serviceGroup = this.fb.group({
      name: ['', Validators.required],
      price: ['', [Validators.required, Validators.min(0)]],
      description: ['']
    });
    this.additionalServices.push(serviceGroup);
  }

  public removeService(index: number) {
    this.additionalServices.removeAt(index);
  }

  public async submit() {
    if (!this.validateAllForms()) {
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    // Process Stripe Payment
    const name = this.paymentForm.get('cardName')?.value;
    this.stripeService
      .createPaymentMethod({
        type: 'card',
        card: this.card.element,
        billing_details: {
          name: name,
        },
      })
      .subscribe(async (result) => {
        if (result.error) {
          this.errorMessage = result.error.message || 'Payment failed';
          this.isLoading = false;
          alert(this.errorMessage);
        } else if (result.paymentMethod) {
          await this.completeRegistration(result.paymentMethod.id);
        }
      });
  }

  private async completeRegistration(paymentMethodId: string) {
    try {
      console.log('🚀 Starting registration process...');
      
      // Clear ALL tokens before registration
      this.clearAllStorage();
      console.log('Tokens cleared');
      
      // 1. REGISTER DOCTOR FIRST (Get medecin ID)
      const userRequest = this.prepareUserData();
      console.log('Registering doctor...', { 
        ...userRequest, 
        password: '[HIDDEN]',
        signature: userRequest.signature ? '[BASE64_IMAGE]' : null 
      });
      
      const userResponse = await this.utilisateurService.registerMedecin(userRequest).toPromise();
      
      if (!userResponse?.idUtilisateur) {
        throw new Error('Doctor registration failed - no user ID returned');
      }
      
      console.log('✅ Doctor registered, ID:', userResponse.idUtilisateur);
      
      // 2. CREATE CABINET WITH MEDECIN ID
      const cabinetData = this.prepareCabinetData();
      console.log('Creating cabinet for medecin:', userResponse.idUtilisateur);
      
      const cabinetResponse = await this.cabinetService.createCabinet(cabinetData).toPromise();
      
      if (!cabinetResponse?.id) {
        throw new Error('Cabinet creation failed - no ID returned');
      }
      
      console.log('✅ Cabinet created, ID:', cabinetResponse.id);
      
      // 3. UPDATE DOCTOR WITH CABINET ID
      try {
        await this.utilisateurService.updateUtilisateur(
          userResponse.idUtilisateur, 
          { idCabinet: cabinetResponse.id }
        ).toPromise();
        console.log('✅ Doctor updated with cabinet ID');
      } catch (updateError) {
        console.log('⚠️ Could not update doctor with cabinet ID:', updateError);
      }
      
      // 4. ADD SERVICES
      try {
        await this.addServices(cabinetResponse.id);
        console.log('✅ Services added');
      } catch (serviceError) {
        console.log('⚠️ Services not added:', serviceError);
      }
      
      // 5. Payment processing
      console.log('Payment method ID:', paymentMethodId);
      
      // Store final data
      this.storeFinalData(userResponse, cabinetResponse);
      
      // SUCCESS!
      alert('🎉 Registration complete! Please login with your credentials.');
      this.router.navigate(['/login']);
      
    } catch (error: any) {
      console.error('❌ Registration failed:', error);
      
      // Clear storage on error
      this.clearAllStorage();
      
      if (error.status === 401) {
        alert('Authentication issue. Please try again or clear browser cache.');
      } else {
        const errorMsg = error.error?.message || error.message || 'Unknown error';
        alert('Registration failed: ' + errorMsg);
      }
      
    } finally {
      this.isLoading = false;
    }
  }

  private clearAllStorage(): void {
    localStorage.clear();
    sessionStorage.clear();
  }

  private storeFinalData(user: UtilisateurResponse, cabinet: CabinetResponse): void {
    localStorage.setItem('medecin_data', JSON.stringify(user));
    localStorage.setItem('cabinet_data', JSON.stringify(cabinet));
    localStorage.setItem('medecin_id', user.idUtilisateur.toString());
    localStorage.setItem('cabinet_id', cabinet.id.toString());
  }

  private prepareCabinetData(): any {
    const clinicData = this.clinicForm.value;
    const servicesData = this.servicesForm.value;

    return {
      nom: clinicData.name,
      specialite: clinicData.specialty,
      adresse: clinicData.address,
      tel: clinicData.tel,
      tarifConsultation: parseFloat(servicesData.generalPrice) || 200,
      maxPatientsJour: parseInt(clinicData.maxPatients, 10) || 20,
      dureeConsultation: parseInt(clinicData.consultationTime, 10) || 30,
      logo: clinicData.logo || null,
      abonnement: {
        typePeriode: this.selectedPlan.toLowerCase() === 'monthly' ? 'MENSUEL' : 'ANNUEL',
        montant: this.selectedPlan.toLowerCase() === 'monthly' ? 29.99 : 299.99
      },
      serviceConsultationGenerale: {
        nomService: 'Consultation Générale',
        prix: parseFloat(servicesData.generalPrice) || 200,
        description: 'Consultation médicale générale',
        obligatoire: true
      }
      // medecinId will be added by CabinetService from localStorage
    };
  }

  private prepareUserData(): UtilisateurRequest {
    const personalData = this.personalForm.value;
    
    // Get signature (either drawn or uploaded)
    let signature = null;
    if (this.signatureImage) {
      // If drawn signature
      signature = this.signatureImage;
    } else if (personalData.signature) {
      // If uploaded signature
      signature = personalData.signature;
    }

    return {
      login: personalData.username,
      password: personalData.password,
      nom: personalData.lastName,
      prenom: personalData.firstName,
      numTel: personalData.tel,
      role: 'MEDECIN',
      signature: signature,  // Include signature
      // Don't include idCabinet here - it will be set after cabinet creation
    };
  }
  private async linkUserToCabinet(userId: number, cabinetId: number): Promise<any> {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    
    return this.http.patch(
      `/api/utilisateur/users/${userId}/cabinet`,
      { idCabinet: cabinetId },
      { headers }
    ).toPromise();
  }

  private validateAllForms(): boolean {
    const forms = [this.personalForm, this.clinicForm, this.servicesForm];

    for (const form of forms) {
      if (form.invalid) {
        form.markAllAsTouched();
        return false;
      }
    }

    if (!this.signatureImage && !this.personalForm.get('signature')?.value) {
      alert("Please provide a signature.");
      return false;
    }

    return true;
  }

  

  private async addServices(cabinetId: number): Promise<void> {
    const additionalServices = this.additionalServices.value;
    for (const service of additionalServices) {
      const serviceDTO: ServiceConsultationDTO = {
        nom: service.name,
        prix: parseFloat(service.price),
        description: service.description,
      };
      await this.cabinetService.addService(cabinetId, serviceDTO).toPromise();
    }
  }
}