import { Component, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-doctor-onboarding',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './doctor-onboarding.html',
  styleUrls: ['./doctor-onboarding.css']
})
export class DoctorOnboardingComponent implements AfterViewInit {
  currentStep = 1;
  totalSteps = 4;

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

  constructor(private fb: FormBuilder, private router: Router) {
    this.personalForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', Validators.required], // TODO: Add async validator for uniqueness
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
      consultationTime: ['', Validators.required],
      logo: [null]
    });

    this.servicesForm = this.fb.group({
      generalPrice: ['', Validators.required],
      additionalServices: this.fb.array([])
    });

    this.paymentForm = this.fb.group({
      cardName: ['', Validators.required],
      cardNumber: ['', [Validators.required, Validators.pattern(/^\d{16}$/)]],
      expiry: ['', [Validators.required, Validators.pattern(/^(0[1-9]|1[0-2])\/\d{2}$/)]],
      cvc: ['', [Validators.required, Validators.pattern(/^\d{3,4}$/)]]
    });
  }

  get additionalServices() {
    return this.servicesForm.get('additionalServices') as FormArray;
  }

  ngAfterViewInit() {
    // Canvas might not be available initially if step 1 is not default or logic specific
    // However, since we start at step 1, we can init if mode is draw
    if (this.signatureMode === 'draw') {
      setTimeout(() => this.initCanvas(), 0);
    }
  }

  // Navigation
  nextStep() {
    if (this.currentStep < this.totalSteps) {
      if (this.validateCurrentStep()) {
        this.currentStep++;
        if (this.currentStep === 1 && this.signatureMode === 'draw') {
          setTimeout(() => this.initCanvas(), 100);
        }
      }
    } else {
      this.submit();
    }
  }

  prevStep() {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  validateCurrentStep(): boolean {
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
        if (this.paymentForm.invalid) {
          this.paymentForm.markAllAsTouched();
          return false;
        }
        return true;
      default: return false;
    }
  }

  // Signature Logic
  setSignatureMode(mode: 'upload' | 'draw') {
    this.signatureMode = mode;
    if (mode === 'draw') {
      setTimeout(() => this.initCanvas(), 50);
    }
  }

  initCanvas() {
    if (!this.signatureCanvas) return;
    const canvas = this.signatureCanvas.nativeElement;
    this.canvasContext = canvas.getContext('2d');
    if (this.canvasContext) {
      this.canvasContext.lineWidth = 2;
      this.canvasContext.lineCap = 'round';
      this.canvasContext.strokeStyle = '#000';
    }
  }

  startDrawing(e: MouseEvent | TouchEvent) {
    this.isDrawing = true;
    const pos = this.getEventPos(e);
    this.canvasContext?.beginPath();
    this.canvasContext?.moveTo(pos.x, pos.y);
  }

  draw(e: MouseEvent | TouchEvent) {
    if (!this.isDrawing) return;
    e.preventDefault();
    const pos = this.getEventPos(e);
    this.canvasContext?.lineTo(pos.x, pos.y);
    this.canvasContext?.stroke();
  }

  stopDrawing() {
    if (!this.isDrawing) return;
    this.isDrawing = false;
    this.saveSignature();
  }

  getEventPos(e: MouseEvent | TouchEvent) {
    const canvas = this.signatureCanvas.nativeElement;
    const rect = canvas.getBoundingClientRect();
    const clientX = 'touches' in e ? e.touches[0].clientX : e.clientX;
    const clientY = 'touches' in e ? e.touches[0].clientY : e.clientY;
    return {
      x: clientX - rect.left,
      y: clientY - rect.top
    };
  }

  clearSignature() {
    const canvas = this.signatureCanvas.nativeElement;
    this.canvasContext?.clearRect(0, 0, canvas.width, canvas.height);
    this.signatureImage = null;
  }

  saveSignature() {
    this.signatureImage = this.signatureCanvas.nativeElement.toDataURL();
    this.personalForm.patchValue({ signature: this.signatureImage });
  }

  onFileSelected(event: Event, field: 'signature' | 'logo') {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = () => {
        if (field === 'signature') {
          this.personalForm.patchValue({ signature: reader.result });
          this.signatureImage = reader.result as string;
        } else {
          this.clinicForm.patchValue({ logo: reader.result });
          this.logoPreview = reader.result as string;
        }
      };
      reader.readAsDataURL(file);
    }
  }

  // Services Logic
  addService() {
    const serviceGroup = this.fb.group({
      name: ['', Validators.required],
      price: ['', Validators.required]
    });
    this.additionalServices.push(serviceGroup);
  }

  removeService(index: number) {
    this.additionalServices.removeAt(index);
  }

  submit() {
    if (this.paymentForm.valid) {
      console.log('Submission Complete', {
        personal: this.personalForm.value,
        clinic: this.clinicForm.value,
        services: this.servicesForm.value,
        payment: this.paymentForm.value
      });
      // Simulate API call
      setTimeout(() => {
        alert("Setup Complete! Welcome to Clinic Flow.");
        this.router.navigate(['/']);
      }, 1500);
    }
  }
}
