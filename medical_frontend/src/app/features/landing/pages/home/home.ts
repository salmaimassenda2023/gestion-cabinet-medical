import { Component } from '@angular/core';
import { NavbarComponent } from '../../components/navbar/navbar';
import { HeroSectionComponent } from '../../components/hero-section/hero-section';
import { PatientFeaturesSectionComponent } from '../../components/patient-features-section/patient-features-section';
import { DoctorFeaturesSectionComponent } from '../../components/doctor-features-section/doctor-features-section';
import { UserTypeSectionComponent } from '../../components/user-type-section/user-type-section';
import { FooterComponent } from '../../components/footer/footer';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    NavbarComponent,
    HeroSectionComponent,
    PatientFeaturesSectionComponent,
    DoctorFeaturesSectionComponent,
    UserTypeSectionComponent,
    FooterComponent
  ],
  template: `
    <div class="min-h-screen bg-background">
      <app-navbar></app-navbar>
      <app-hero-section></app-hero-section>
      <app-doctor-features-section></app-doctor-features-section>
      <app-patient-features-section></app-patient-features-section>
      <app-user-type-section></app-user-type-section>
      <app-footer></app-footer>
    </div>
  `,
  styles: [`
    .min-h-screen { min-height: 100vh; }
    .bg-background { background-color: var(--background); }
  `]
})
export class HomeComponent { }
