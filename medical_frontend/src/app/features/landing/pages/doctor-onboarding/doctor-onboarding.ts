import { Component, ElementRef, ViewChild, AfterViewInit, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { CabinetService, CabinetResponse, ServiceConsultationDTO } from '../../../doctor/services/cabinet.service';
import { UtilisateurService, UtilisateurRequest, UtilisateurResponse } from '../../../auth/services/utilisateur.service';
import { ActivatedRoute } from '@angular/router';
import { NgxStripeModule, StripeCardComponent, StripeService } from 'ngx-stripe';
import { StripeCardElementOptions, StripeElementsOptions } from '@stripe/stripe-js';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { finalize } from 'rxjs/operators';

export class CustomValidators {
  static passwordStrength(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;

    const hasUpperCase = /[A-Z]/.test(value);
    const hasLowerCase = /[a-z]/.test(value);
    const hasNumber = /\d/.test(value);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);
    const isValidLength = value.length >= 8;

    const errors: ValidationErrors = {};
    
    if (!hasUpperCase) errors['missingUpperCase'] = true;
    if (!hasLowerCase) errors['missingLowerCase'] = true;
    if (!hasNumber) errors['missingNumber'] = true;
    if (!hasSpecialChar) errors['missingSpecialChar'] = true;
    if (!isValidLength) errors['minLength'] = true;

    return Object.keys(errors).length ? errors : null;
  }

  static phoneNumber(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    if (!value) return null;
    
    const phoneRegex = /^[+]?[1-9][0-9]{7,14}$/;
    return phoneRegex.test(value.replace(/[\s\-()]/g, '')) ? null : { invalidPhone: true };
  }

  static signatureRequired(control: AbstractControl): ValidationErrors | null {
    const value = control.value;
    return value ? null : { signatureRequired: true };
  }
}

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
  loadingMessage = '';
  successMessage: string | null = null;
  errorMessage: string | null = null;
  selectedPlan: string = 'monthly';

  // Dialog state
  showSuccessDialog = false;
  showErrorDialog = false;
  dialogTitle = '';
  dialogMessage = '';
  dialogAction = '';

  // Stripe
  @ViewChild(StripeCardComponent) card!: StripeCardComponent;
  cardOptions: StripeCardElementOptions = {
    style: {
      base: {
        iconColor: '#4F46E5',
        color: '#374151',
        lineHeight: '40px',
        fontWeight: '400',
        fontFamily: '"Inter", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
        fontSize: '16px',
        '::placeholder': {
          color: '#9CA3AF'
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
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-ZÀ-ÿ\\s-]*$')]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.pattern('^[a-zA-ZÀ-ÿ\\s-]*$')]],
      username: ['', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(30),
        Validators.pattern('^[a-zA-Z0-9_]*$')
      ]],
      password: ['', [
        Validators.required,
        Validators.minLength(8),
        CustomValidators.passwordStrength
      ]],
      tel: ['', [Validators.required, CustomValidators.phoneNumber]],
      signature: [null, CustomValidators.signatureRequired]
    });

    this.clinicForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      specialty: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      address: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(200)]],
      tel: ['', [Validators.required, CustomValidators.phoneNumber]],
      maxPatients: ['', [Validators.required, Validators.min(1), Validators.max(100)]],
      consultationTime: ['', [Validators.required, Validators.min(5), Validators.max(120)]],
      logo: [null]
    });

    this.servicesForm = this.fb.group({
      generalPrice: ['', [
        Validators.required,
        Validators.min(50),
        Validators.max(10000),
        Validators.pattern('^[0-9]+(\.[0-9]{1,2})?$')
      ]],
      additionalServices: this.fb.array([])
    });

    this.paymentForm = this.fb.group({
      cardName: ['', [Validators.required, Validators.pattern('^[a-zA-Z\\s]*$')]]
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

  // Dialog Methods
  private showDialog(type: 'success' | 'error', title: string, message: string, action?: string) {
    this.dialogTitle = title;
    this.dialogMessage = message;
    this.dialogAction = action || '';
    
    if (type === 'success') {
      this.showSuccessDialog = true;
      this.successMessage = message;
    } else {
      this.showErrorDialog = true;
      this.errorMessage = message;
    }
    
    this.cd.detectChanges();
  }

  public closeDialog(type: 'success' | 'error') {
    if (type === 'success') {
      this.showSuccessDialog = false;
      this.successMessage = null;
    } else {
      this.showErrorDialog = false;
      this.errorMessage = null;
    }
  }

  // Enhanced Validation Messages
  getValidationMessage(control: AbstractControl | null, fieldName: string): string {
    if (!control || !control.errors || !control.touched) return '';
    
    const errors = control.errors;
    
    if (errors['required']) return `${fieldName} is required`;
    if (errors['minlength']) return `${fieldName} must be at least ${errors['minlength'].requiredLength} characters`;
    if (errors['maxlength']) return `${fieldName} cannot exceed ${errors['maxlength'].requiredLength} characters`;
    if (errors['min']) return `${fieldName} must be at least ${errors['min'].min}`;
    if (errors['max']) return `${fieldName} cannot exceed ${errors['max'].max}`;
    if (errors['pattern']) {
      switch (fieldName) {
        case 'Username': return 'Username can only contain letters, numbers, and underscores';
        case 'Phone': return 'Please enter a valid phone number';
        case 'Name': return 'Please enter a valid name (letters, spaces, and hyphens only)';
        case 'Card Name': return 'Card name can only contain letters and spaces';
        case 'Price': return 'Please enter a valid price (e.g., 150 or 150.50)';
        default: return 'Invalid format';
      }
    }
    if (errors['missingUpperCase']) return 'Password must contain at least one uppercase letter';
    if (errors['missingLowerCase']) return 'Password must contain at least one lowercase letter';
    if (errors['missingNumber']) return 'Password must contain at least one number';
    if (errors['missingSpecialChar']) return 'Password must contain at least one special character';
    if (errors['invalidPhone']) return 'Please enter a valid international phone number';
    if (errors['signatureRequired']) return 'Signature is required';
    
    return 'Invalid value';
  }

getPasswordStrength(control: AbstractControl | null): { strength: 'weak' | 'medium' | 'strong', message: string } {
    if (!control || !control.value) {
        return { strength: 'weak', message: 'Enter password' };
    }
    
    if (!control.errors) {
        return { strength: 'strong', message: 'Strong password' };
    }
    
    const passwordErrors = Object.keys(control.errors).filter(
        error => !['required'].includes(error)
    );
    const errorCount = passwordErrors.length;
    
    if (errorCount >= 3) return { strength: 'weak', message: 'Weak password' };
    if (errorCount >= 2) return { strength: 'medium', message: 'Medium strength' };
    if (errorCount >= 1) return { strength: 'strong', message: 'Good password' };
    
    return { strength: 'strong', message: 'Strong password' };
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
          this.showDialog('error', 'Missing Information', 'Please complete all required personal information fields.');
          return false;
        }
        if (!this.signatureImage && !this.personalForm.get('signature')?.value) {
          this.showDialog('error', 'Signature Required', 'Please provide your signature before proceeding.');
          return false;
        }
        return true;
      case 2:
        if (this.clinicForm.invalid) {
          this.clinicForm.markAllAsTouched();
          this.showDialog('error', 'Clinic Information', 'Please complete all clinic details before proceeding.');
          return false;
        }
        return true;
      case 3:
        if (this.servicesForm.invalid) {
          this.servicesForm.markAllAsTouched();
          this.showDialog('error', 'Services & Pricing', 'Please check your services and pricing information.');
          return false;
        }
        return true;
      case 4:
        if (this.paymentForm.invalid) {
          this.paymentForm.markAllAsTouched();
          this.showDialog('error', 'Payment Details', 'Please complete your payment information.');
          return false;
        }
        return true;
      default: return false;
    }
  }

  // Signature Logic (unchanged)
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
      this.canvasContext.strokeStyle = '#374151';
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
    this.personalForm.get('signature')?.setValue(null);
  }

  private saveSignature() {
    this.signatureImage = this.signatureCanvas.nativeElement.toDataURL();
    this.personalForm.patchValue({ signature: this.signatureImage });
    this.cd.detectChanges();
  }

  public onFileSelected(event: Event, field: 'signature' | 'logo') {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      const reader = new FileReader(); 
      reader.onload = () => {
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

  // Enhanced Services Logic
  public addService() {
    const serviceGroup = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      price: ['', [
        Validators.required,
        Validators.min(0),
        Validators.max(10000),
        Validators.pattern('^[0-9]+(\.[0-9]{1,2})?$')
      ]],
      description: ['', Validators.maxLength(200)]
    });
    this.additionalServices.push(serviceGroup);
    this.cd.detectChanges();
  }

  public removeService(index: number) {
    this.additionalServices.removeAt(index);
    this.cd.detectChanges();
  }

  public async submit() {
    if (!this.validateAllForms()) {
      return;
    }

    this.isLoading = true;
    this.loadingMessage = 'Processing your registration...';
    this.errorMessage = null;
    this.successMessage = null;

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
          this.isLoading = false;
          this.showDialog('error', 'Payment Failed', 
            result.error.message || 'Unable to process payment. Please check your card details and try again.',
            'Try Again');
        } else if (result.paymentMethod) {
          this.loadingMessage = 'Setting up your clinic account...';
          await this.completeRegistration(result.paymentMethod.id);
        }
      });
  }

private async completeRegistration(paymentMethodId: string) {
    try {
        console.log('🚀 Starting registration process...');
        
        // Clear storage to remove any old data
        const keysToRemove = ['medecin_data', 'cabinet_data', 'medecin_id', 'cabinet_id', 'auth_token'];
        keysToRemove.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });
        
        // 1. Register doctor in YOUR database first
        this.loadingMessage = 'Creating your doctor profile...';
        const userRequest = this.prepareUserData();
        
        // Add explicit headers to bypass JWT interceptor
        const headers = new HttpHeaders({
            'Content-Type': 'application/json',
            'X-Skip-Interceptor': 'true'  
        });
        
        const userResponse = await this.http.post<UtilisateurResponse>(
            'http://localhost:8222/api/utilisateur/users/register/medecin',
            userRequest,
            { headers }
        ).toPromise();
        
        if (!userResponse?.idUtilisateur) {
            throw new Error('Doctor registration failed - no user ID returned');
        }
        
        console.log('✅ Doctor registered in database, ID:', userResponse.idUtilisateur);
        
        // 2. Wait for database transaction to commit
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // 3. Create cabinet WITH the medecinId
        this.loadingMessage = 'Setting up your clinic...';
        const cabinetData = this.prepareCabinetData();
        
        // CRITICAL: Add medecinId to cabinet data
        const cabinetWithDoctor = {
            ...cabinetData,
            medecinId: userResponse.idUtilisateur  
        };
        
        console.log('📦 Creating cabinet with data:', cabinetWithDoctor);
        
        const cabinetResponse = await this.http.post<CabinetResponse>(
            'http://localhost:8222/api/cabinet',
            cabinetWithDoctor
        ).toPromise();
        
        if (!cabinetResponse?.id) {
            throw new Error('Cabinet creation failed - no cabinet ID returned');
        }
        
        console.log('✅ Cabinet created, ID:', cabinetResponse.id);
        
        // 4. Wait for cabinet creation to commit
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 5. Update doctor with cabinet ID (with proper headers)
        this.loadingMessage = 'Finalizing your account...';
        
        const syncHeaders = new HttpHeaders({
            'Content-Type': 'application/json',
            'X-Skip-Interceptor': 'true'  
        });
        
        try {
            await this.http.put<void>(
                `http://localhost:8222/api/utilisateur/users/${userResponse.idUtilisateur}/cabinet?idCabinet=${cabinetResponse.id}`,
                {},
                { headers: syncHeaders }
            ).toPromise();
            
            console.log('✅ Doctor updated with cabinet ID');
        } catch (syncError) {
            console.warn('⚠️ Could not sync cabinet ID to doctor (may be handled by backend):', syncError);
        }
        
        // 6. Store final data
        this.storeFinalData(userResponse, cabinetResponse);
        
        // 7. SUCCESS
        this.isLoading = false;
        this.showDialog('success', 'Registration Complete!', 
          'Your clinic has been set up successfully. You can now log in to your account.',
          'Go to Login');
        
    } catch (error: any) {
        console.error('❌ Registration failed:', error);
        
        if (error.error) {
            console.error('Error details:', error.error);
        }
        
        // Clear any partial data
        const keysToRemove = ['medecin_data', 'cabinet_data', 'medecin_id', 'cabinet_id', 'temp_user_id', 'temp_cabinet_id'];
        keysToRemove.forEach(key => {
            localStorage.removeItem(key);
            sessionStorage.removeItem(key);
        });
        
        this.isLoading = false;
        
        let errorMessage = 'Registration failed. Please try again.';
        
        if (error.status === 401) {
            errorMessage = 'Authentication error. Please refresh the page and try again.';
        } else if (error.status === 409 || error.error?.message?.includes('existe déjà')) {
            errorMessage = 'This username is already taken. Please choose a different username.';
            // Go back to step 1 to change username
            this.currentStep = 1;
        } else if (error.error?.message) {
            errorMessage = error.error.message;
        } else if (error.message) {
            errorMessage = error.message;
        }
        
        this.showDialog('error', 'Registration Failed', errorMessage, 'Try Again');
    }
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
        montant: this.selectedPlan.toLowerCase() === 'monthly' ? 350 : 3000
      },
      serviceConsultationGenerale: {
        nomService: 'Consultation Générale',
        prix: parseFloat(servicesData.generalPrice) || 200,
        description: 'Consultation médicale générale',
        obligatoire: true
      }
    };
  }

  private prepareUserData(): any {
    const personalData = this.personalForm.value;
    
    let phoneNumber = personalData.tel.trim();
    phoneNumber = phoneNumber.replace(/[\s\-()]/g, '');
    if (phoneNumber.startsWith('212') && !phoneNumber.startsWith('+')) {
        phoneNumber = '+' + phoneNumber;
    }
    
    let signature = null;
    if (this.signatureImage) {
        signature = this.signatureImage;
    } else if (personalData.signature) {
        signature = personalData.signature;
    }
    
    if (signature && signature.startsWith('data:image')) {
        signature = signature.split(',')[1];
    }
    
    return {
        login: personalData.username.trim(),
        password: personalData.password,
        nom: personalData.lastName.trim(),
        prenom: personalData.firstName.trim(),
        numTel: phoneNumber,
        role: 'MEDECIN', 
        signature: signature,
    };
}

  private validateAllForms(): boolean {
    const forms = [this.personalForm, this.clinicForm, this.servicesForm];

    for (const form of forms) {
      if (form.invalid) {
        form.markAllAsTouched();
        this.showDialog('error', 'Missing Information', 
          'Please complete all required fields before submitting.',
          'Review Form');
        return false;
      }
    }

    if (!this.signatureImage && !this.personalForm.get('signature')?.value) {
      this.showDialog('error', 'Signature Required', 
        'Please provide your signature to complete registration.',
        'Add Signature');
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

  // Dialog actions
  public onDialogAction() {
    if (this.showSuccessDialog) {
      this.router.navigate(['/login']);
    } else if (this.showErrorDialog) {
      this.closeDialog('error');
    }
  }
  private storeFinalData(user: UtilisateurResponse, cabinet: CabinetResponse): void {
    const completeUserData = {
        ...user,
        idCabinet: cabinet.id  
    };
    
    localStorage.setItem('medecin_data', JSON.stringify(completeUserData));
    localStorage.setItem('cabinet_data', JSON.stringify(cabinet));
    localStorage.setItem('medecin_id', user.idUtilisateur.toString());
    localStorage.setItem('cabinet_id', cabinet.id.toString());
    
    console.log('💾 Final data stored:', {
        medecinId: user.idUtilisateur,
        cabinetId: cabinet.id
    });
}
}