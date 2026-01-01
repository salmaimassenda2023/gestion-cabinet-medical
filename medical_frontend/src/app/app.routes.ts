import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { HomeComponent } from './features/landing/pages/home/home';
import { UserTypeSectionComponent } from './features/landing/components/user-type-section/user-type-section';
import { DoctorOnboardingComponent } from './features/landing/pages/doctor-onboarding/doctor-onboarding';
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard.component';
import { DoctorDashboardComponent } from './features/doctor/doctor-dashboard.component';
import { ConsultationComponent } from './features/doctor/consultation/consultation';
import { SecretaryManagementComponent } from './features/doctor/secretary-management/secretary-management';
import { DoctorProfileComponent } from './features/doctor/profile/profile';
import { SecretaryDashboardComponent } from './features/secretary/secretary-dashboard.component';
import { UsersComponent } from './features/admin/users/users';
import { ClinicsComponent } from './features/admin/clinics/clinics';
import { MedicamentsComponent } from './features/admin/medicaments/medicaments';
import { PaymentsComponent } from './features/admin/payments/payments';
import { ProfileComponent } from './features/admin/profile/profile';
import { SecretaryCalendarComponent } from './features/secretary/calendar/calendar';
import { SecretaryPatientsComponent } from './features/secretary/patients/patients';
import { SecretaryPaymentsComponent } from './features/secretary/payments/payments';
import { SecretaryOrdersComponent } from './features/secretary/orders/orders';
import { SecretaryProfileComponent } from './features/secretary/profile/profile';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'usertype', component: UserTypeSectionComponent },
  { path: 'onboarding', component: DoctorOnboardingComponent },
  { path: 'admin', component: AdminDashboardComponent },
  { path: 'admin/users', component: UsersComponent },
  { path: 'admin/clinics', component: ClinicsComponent },
  { path: 'admin/medicaments', component: MedicamentsComponent },
  { path: 'admin/payments', component: PaymentsComponent },
  { path: 'admin/profile', component: ProfileComponent },
  { path: 'doctor', component: DoctorDashboardComponent },
  { path: 'doctor/consultation', component: ConsultationComponent },
  { path: 'doctor/secretary-management', component: SecretaryManagementComponent },
  { path: 'doctor/profile', component: DoctorProfileComponent },
  {
    path: 'secretary',
    component: SecretaryDashboardComponent,
    children: [
      { path: '', redirectTo: 'calendar', pathMatch: 'full' },
      { path: 'calendar', component: SecretaryCalendarComponent },
      { path: 'orders', component: SecretaryOrdersComponent },
      { path: 'patients', component: SecretaryPatientsComponent },
      { path: 'payments', component: SecretaryPaymentsComponent },
      { path: 'profile', component: SecretaryProfileComponent },
    ]
  },
  { path: '**', redirectTo: '' }
];