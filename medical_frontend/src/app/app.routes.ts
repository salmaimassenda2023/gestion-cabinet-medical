import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

// Auth & Landing
import { LoginComponent } from './features/auth/login/login';
import { HomeComponent } from './features/landing/pages/home/home';
import { UserTypeSectionComponent } from './features/landing/components/user-type-section/user-type-section';
import { DoctorOnboardingComponent } from './features/landing/pages/doctor-onboarding/doctor-onboarding';

// Admin
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard.component';
import { UsersComponent } from './features/admin/users/users';
import { ClinicsComponent } from './features/admin/clinics/clinics';
import { PaymentsComponent } from './features/admin/payments/payments';
import { ProfileComponent } from './features/admin/profile/profile';

// Doctor
import { DoctorDashboardComponent } from './features/doctor/doctor-dashboard.component';
import { ConsultationComponent } from './features/doctor/consultation/consultation';
import { MedicalDossierComponent } from './features/doctor/consultation/medical-dossier/medical-dossier/medical-dossier';
import { SecretaryManagementComponent } from './features/doctor/secretary-management/secretary-management';
import { DoctorProfileComponent } from './features/doctor/profile/profile';

// Secretary
import { SecretaryDashboardComponent } from './features/secretary/secretary-dashboard.component';
import { SecretaryCalendarComponent } from './features/secretary/calendar/calendar';
import { SecretaryPatientsComponent } from './features/secretary/patients/patients';
import { SecretaryOrdersComponent } from './features/secretary/orders/orders';
import { SecretaryProfileComponent } from './features/secretary/profile/profile';
import { SecretaryPaymentComponent } from './features/secretary/payments/payments';

export const routes: Routes = [

  // PUBLIC ROUTES (No Authentication Required)
  { 
    path: '', 
    component: HomeComponent 
  },
  { 
    path: 'login', 
    component: LoginComponent 
  },
  { 
    path: 'usertype', 
    component: UserTypeSectionComponent 
  },
  { 
    path: 'onboarding', 
    component: DoctorOnboardingComponent 
  },
  {
    path: 'unauthorized',
    loadComponent: () => import('./pages/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent)
  },

  // ADMIN ROUTES (ADMIN & SUPER_ADMIN Only)
  { 
    path: 'admin', 
    component: AdminDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  { 
    path: 'admin/users', 
    component: UsersComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  { 
    path: 'admin/clinics', 
    component: ClinicsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  { 
    path: 'admin/payments', 
    component: PaymentsComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN'] }
  },
  { 
    path: 'admin/profile', 
    component: ProfileComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'SUPER_ADMIN'] }
  },

  // DOCTOR ROUTES (MEDECIN Only)
  { 
    path: 'doctor', 
    component: DoctorDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['MEDECIN'] }
  },
  { 
    path: 'doctor/consultation', 
    component: ConsultationComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['MEDECIN'] }
  },
  {
    path: 'patient/:id/dossier',
    component: MedicalDossierComponent,
    data: { title: 'Medical Dossier' }
  },
  { 
    path: 'doctor/secretary-management', 
    component: SecretaryManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['MEDECIN'] }
  },
  { 
    path: 'doctor/profile', 
    component: DoctorProfileComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['MEDECIN'] }
  },

  // SECRETARY ROUTES 
  {
    path: 'secretary',
    component: SecretaryDashboardComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['SECRETAIRE','MEDECIN'] },
    children: [
      { 
        path: '', 
        redirectTo: 'calendar', 
        pathMatch: 'full' 
      },
      { 
        path: 'calendar', 
        component: SecretaryCalendarComponent 
      },
      { 
        path: 'orders', 
        component: SecretaryOrdersComponent 
      },
      { 
        path: 'patients', 
        component: SecretaryPatientsComponent 
      },
      { 
        path: 'payments', 
        component: SecretaryPaymentComponent 
      },
      { 
        path: 'profile', 
        component: SecretaryProfileComponent 
      },
    ]
  },

  // FALLBACK
  { 
    path: '**', 
    redirectTo: '' 
  }
];