import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { IncomeChartComponent } from './income-chart.component';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.html',
  styleUrls: ['./admin-dashboard.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, IncomeChartComponent, RouterModule]
})
export class AdminDashboardComponent implements OnInit {
  logoPath = 'assets/logo.png';

  stats = [
    { title: 'Clinics', value: '120', subtitle: 'work with us', icon: 'building' },
    { title: 'Revenue', value: '43,000 DH', subtitle: 'in month', icon: 'dollar-sign' },
    { title: 'Medicament', value: '570', subtitle: 'of medicament', icon: 'pills' }
  ];

  incomeData = [
    { name: '5k', value: 45 },
    { name: '10k', value: 40 },
    { name: '15k', value: 48 },
    { name: '20k', value: 85 },
    { name: '25k', value: 55 },
    { name: '30k', value: 40 },
    { name: '35k', value: 35 },
    { name: '40k', value: 50 },
    { name: '45k', value: 70 },
    { name: '50k', value: 55 },
    { name: '55k', value: 45 },
    { name: '60k', value: 38 },
  ];

  notifications = [
    { id: 1, title: 'New appointment scheduled', time: '2 min ago', icon: 'calendar' },
    { id: 2, title: 'Report ready for review', time: '15 min ago', icon: 'file-alt' },
    { id: 3, title: 'Low stock alert: Paracetamol', time: '1 hour ago', icon: 'exclamation-circle' },
    { id: 4, title: 'Payment received', time: '3 hours ago', icon: 'dollar-sign' },
  ];



  showNotifications = false;
  activeNavIcon = 'building';

  selectedMonth = 'october';

  constructor(private adminService: AdminService) { }

  ngOnInit(): void {
    this.adminService.getDashboardStats().subscribe(stats => {
      this.stats[0].value = stats.clinics.toString();
      this.stats[1].value = `${stats.revenue} DH`;
      this.stats[2].value = stats.medicaments.toString();
    });

    this.adminService.getIncomeData().subscribe(data => {
      this.incomeData = data;
    });
  }

  onNavIconClick(icon: string): void {
    this.activeNavIcon = icon;
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
  }

  logout(): void {
    // In a real app, logic to clear tokens would go here
    console.log('Logging out...');
    window.location.href = '/login';
  }

  getIconClass(iconName: string): string {
    const iconMap: { [key: string]: string } = {
      'building': 'fas fa-building',
      'dollar-sign': 'fas fa-dollar-sign',
      'pills': 'fas fa-pills',
      'users': 'fas fa-users',
      'bell': 'fas fa-bell',
      'user': 'fas fa-user',
      'calendar': 'fas fa-calendar',
      'file-alt': 'fas fa-file-alt',
      'file-text': 'fas fa-file-alt',
      'exclamation-circle': 'fas fa-exclamation-circle',
      'alert-circle': 'fas fa-exclamation-circle',
      'stethoscope': 'fas fa-stethoscope',
      'camera': 'fas fa-camera'
    };

    return iconMap[iconName] || 'fas fa-question';
  }
}