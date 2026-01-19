import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HeaderComponent } from '../../shared/components/header/header.component';
import { IncomeChartComponent } from '../admin/admin-dashboard/income-chart.component';
import { NotificationBellComponent } from '../../shared/components/Notification-bell/notification-bell/notification-bell';
import { PatientService } from '../../core/services/patient.service';
import { RendezvousService } from '../../core/services/rendezvous.service';
import { PaiementService } from '../../core/services/paiement.service';
import { UtilisateurService } from '../auth/services/utilisateur.service';
import { forkJoin } from 'rxjs';
@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    HeaderComponent,
    IncomeChartComponent,
    NotificationBellComponent
  ],
  templateUrl: './doctor-dashboard.html',
  styleUrls: ['./doctor-dashboard.css']
})
export class DoctorDashboardComponent implements OnInit {
  currentDate = new Date();
  logoPath = 'assets/logo.png';
  doctorName = 'Farah';

  statsItems = [
    { title: 'Total Patients', value: '1,250', subtitle: 'registered', icon: 'users' },
    { title: "Today's Patients", value: '15', subtitle: 'scheduled', icon: 'calendar-day' },
    { title: 'Monthly Revenue', value: '45,000 MAD', subtitle: 'this month', icon: 'wallet' }
  ];

  notifications = [
    { id: 1, title: 'New appointment scheduled', time: '2 min ago', icon: 'calendar' },
    { id: 2, title: 'Patient record updated', time: '15 min ago', icon: 'file-alt' },
    { id: 3, title: 'Secretary message: Urgent', time: '1 hour ago', icon: 'envelope' },
  ];

  selectedMonth = 'october';
  weekDays = ['S', 'M', 'T', 'W', 'T', 'F', 'S'];
  yearCalendar: { name: string, days: { day: number | null, isToday: boolean }[] }[] = [];
  currentYear: number = new Date().getFullYear();
  isLoadingStats = false;

  constructor(
    private patientService: PatientService,
    private rendezvousService: RendezvousService,
    private paiementService: PaiementService,
    private utilisateurService: UtilisateurService
  ) { }

  ngOnInit() {
    this.generateYearCalendar();
    this.loadStats();
    this.loadDoctorInfo();
  }

  loadDoctorInfo() {
    this.utilisateurService.getCurrentUser().subscribe({
      next: (user) => {
        this.doctorName = user.nom || 'Doctor';
      },
      error: (err) => console.error('Error loading doctor info:', err)
    });
  }

  loadStats() {
    this.isLoadingStats = true;

    this.utilisateurService.getCurrentUser().subscribe({
      next: (user) => {
        const medecinId = user.idUtilisateur;
        const cabinetId = user.idCabinet || 1;

        forkJoin({
          patients: this.patientService.getPatientsByCabinet(cabinetId),
          appointments: this.rendezvousService.getRendezVousDuJour(medecinId),
          factures: this.paiementService.getFacturesByCabinet(cabinetId)
        }).subscribe({
          next: ({ patients, appointments, factures }) => {
            // Total Patients
            this.statsItems[0].value = patients.length.toString();

            // Today's Patients
            this.statsItems[1].value = appointments.length.toString();

            // Monthly Revenue
            const now = new Date();
            const currentMonth = now.getMonth();
            const currentYear = now.getFullYear();

            const monthlyRevenue = factures
              .filter(f => {
                const fDate = new Date(f.dateFacture);
                return fDate.getMonth() === currentMonth &&
                  fDate.getFullYear() === currentYear &&
                  f.statut === 'PAYEE';
              })
              .reduce((sum, f) => sum + (f.montantTotal || 0), 0);

            this.statsItems[2].value = `${monthlyRevenue.toLocaleString()} MAD`;

            this.isLoadingStats = false;
          },
          error: (err) => {
            console.error('Error loading dashboard stats:', err);
            this.isLoadingStats = false;
          }
        });
      },
      error: (err) => {
        console.error('Error getting current user for stats:', err);
        this.isLoadingStats = false;
      }
    });
  }

  generateYearCalendar() {
    const now = new Date();
    const year = now.getFullYear();
    this.currentYear = year;
    this.yearCalendar = [];

    const monthNames = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    for (let m = 0; m < 12; m++) {
      const days = [];
      const firstDay = new Date(year, m, 1).getDay();
      const daysInMonth = new Date(year, m + 1, 0).getDate();

      // Padding for previous month days (0 is Sunday)
      for (let i = 0; i < firstDay; i++) {
        days.push({ day: null, isToday: false });
      }

      // Current month days
      for (let d = 1; d <= daysInMonth; d++) {
        days.push({
          day: d,
          isToday: d === now.getDate() && m === now.getMonth() && year === now.getFullYear()
        });
      }

      this.yearCalendar.push({
        name: monthNames[m],
        days: days
      });
    }
  }

  getIconClass(icon: string): string {
    const iconMap: { [key: string]: string } = {
      'users': 'fas fa-users',
      'calendar-day': 'fas fa-calendar-day',
      'wallet': 'fas fa-wallet',
      'calendar': 'fas fa-calendar',
      'file-alt': 'fas fa-file-alt',
      'envelope': 'fas fa-envelope'
    };
    return iconMap[icon] || 'fas fa-circle';
  }

  onLogout() {
    console.log('Logging out...');
  }

  onSearch(term: string) {
    console.log('Searching for:', term);
  }
}
