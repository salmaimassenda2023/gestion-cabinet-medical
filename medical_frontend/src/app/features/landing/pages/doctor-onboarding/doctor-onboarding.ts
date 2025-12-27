import { Component, ElementRef, ViewChild, AfterViewInit, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';
import { CabinetService, CabinetResponse, ServiceConsultationDTO } from '../../../doctor/services/cabinet.service';
import { UtilisateurService, UtilisateurRequest, UtilisateurResponse } from '../../../auth/services/utilisateur.service';
import { ActivatedRoute } from '@angular/router';
import { NgxStripeModule, StripeCardComponent, StripeService } from 'ngx-stripe';
import { StripeCardElementOptions, StripeElementsOptions } from '@stripe/stripe-js';
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

  // Step 4: Payment (Consider removing or making optional in real implementation)
  paymentForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private cabinetService: CabinetService,
    private utilisateurService: UtilisateurService,
    private cd: ChangeDetectorRef,
    private route: ActivatedRoute,
    private stripeService: StripeService
  ) {
    this.personalForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', Validators.required], // Will be used as login
      password: ['', [Validators.required, Validators.minLength(8)]],
      tel: ['', Validators.required],
      signature: [null] // Will hold base64 string or file
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

    // Note: Payment handled by Stripe
    this.paymentForm = this.fb.group({
      cardName: ['', Validators.required]
    });
  }

  get additionalServices() {
    return this.servicesForm.get('additionalServices') as FormArray;
  }

  ngOnInit() {
    // You might want to check if user is already registered
    // and redirect if needed
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
          console.log('Clinic Form is invalid:', this.clinicForm);
          Object.keys(this.clinicForm.controls).forEach(key => {
            const control = this.clinicForm.get(key);
            if (control?.invalid) {
              console.log(`Invalid control: ${key}, value: ${control.value}, errors:`, control.errors);
            }
          });
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
        // Payment might be optional in your case
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
          // Show error to your customer
          this.errorMessage = result.error.message || 'Payment failed';
          this.isLoading = false;
          alert(this.errorMessage);
        } else if (result.paymentMethod) {
          // Send the paymentMethod.id to your server
          await this.completeRegistration(result.paymentMethod.id);
        }
      });
  }

  private async completeRegistration(paymentMethodId: string) {
    try {
      // 1. Create Cabinet first
      const cabinetData = this.prepareCabinetData();
      const cabinetResponse = await this.cabinetService.createCabinet(cabinetData).toPromise();

      if (!cabinetResponse) {
        throw new Error('Failed to create cabinet. Please check clinic information.');
      }

      // 2. Register Doctor/User with cabinet ID
      const userRequest = this.prepareUserData(cabinetResponse.idCabinet);
      const userResponse = await this.utilisateurService.registerMedecin(userRequest).toPromise();

      if (!userResponse) {
        throw new Error('Failed to register doctor. This might be due to an existing username.');
      }

      // 3. Add Services
      await this.addServices(cabinetResponse.idCabinet);

      // 4. Send Payment Info (Mock call as backend might not have endpoint yet)
      console.log('Sending token to backend:', paymentMethodId, 'Plan:', this.selectedPlan);
      // await this.paymentService.processPayment(paymentMethodId, this.selectedPlan, ...);

      // 5. Complete the process
      console.log('Registration Complete', {
        cabinet: cabinetResponse,
        user: userResponse
      });

      alert("Setup Complete! Welcome to Clinic Flow.");
      this.router.navigate(['/dashboard']); // Redirect to dashboard or login

    } catch (error: any) {
      console.error('Registration failed:', error);
      this.errorMessage = error.error?.message || error.message || 'Registration failed. Please try again.';
      alert(this.errorMessage);
    } finally {
      this.isLoading = false;
    }
  }

  private validateAllForms(): boolean {
    // Validate all forms except payment which might be optional
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

  private prepareCabinetData(): any {
    const clinicData = this.clinicForm.value;
    const servicesData = this.servicesForm.value;

    return {
      nom: clinicData.name,
      specialite: clinicData.specialty,
      adresse: clinicData.address,
      numTel: clinicData.tel,
      emailContact: clinicData.emailContact,
      tarifConsultation: parseFloat(servicesData.generalPrice),
      maxPatientsJour: parseInt(clinicData.maxPatients, 10),
      dureeConsultation: parseInt(clinicData.consultationTime, 10),
      logo: clinicData.logo || null
    };
  }

  private prepareUserData(cabinetId: number): UtilisateurRequest {
    const personalData = this.personalForm.value;

    return {
      login: personalData.username,
      password: personalData.password,
      nom: personalData.lastName,
      prenom: personalData.firstName,
      numTel: personalData.tel,
      role: 'MEDECIN',
      idCabinet: cabinetId
    };
  }

  private async addServices(cabinetId: number): Promise<void> {
    const generalService: ServiceConsultationDTO = {
      nom: 'Consultation Générale',
      prix: parseFloat(this.servicesForm.value.generalPrice),
      description: 'Consultation médicale générale'
    };

    // Add general consultation service
    await this.cabinetService.addService(cabinetId, generalService).toPromise();

    // Add additional services
    const additionalServices = this.additionalServices.value;
    for (const service of additionalServices) {
      const serviceDTO: ServiceConsultationDTO = {
        nom: service.name,
        prix: parseFloat(service.price),
        description: service.description
      };
      await this.cabinetService.addService(cabinetId, serviceDTO).toPromise();
    }
  }

}