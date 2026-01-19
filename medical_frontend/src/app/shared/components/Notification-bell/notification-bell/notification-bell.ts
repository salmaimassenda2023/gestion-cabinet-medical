import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, PatientSuivantNotification } from '../../../../core/services/notification.service';
import { Router } from '@angular/router';
import { UtilisateurService } from '../../../../features/auth/services/utilisateur.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notification-bell.html',
  styleUrls: ['./notification-bell.css']
})
export class NotificationBellComponent implements OnInit, OnDestroy {
  notifications: PatientSuivantNotification[] = [];
  unreadCount: number = 0;
  showDropdown: boolean = false;
  private subscriptions: Subscription[] = [];
  private medecinId!: number;

  constructor(
    private notificationService: NotificationService,
    private utilisateurService: UtilisateurService,
    private router: Router
  ) { }

  ngOnInit() {
    this.loadCurrentUser();
    this.setupClickOutsideListener();
  }

  private loadCurrentUser() {
    this.utilisateurService.getCurrentUser().subscribe({
      next: (user) => {
        if (user.role === 'MEDECIN' && user.idUtilisateur) {
          this.medecinId = user.idUtilisateur;
          this.setupNotifications();
        }
      }
    });
  }

  private setupNotifications() {
    // Initialize notification service for this user
    this.notificationService.initializeForUser(this.medecinId);

    // Subscribe to notifications
    const notificationSub = this.notificationService.notifications$.subscribe(
      notifications => {
        this.notifications = notifications;
        console.log('🔔 Notifications updated:', notifications.length);
      }
    );

    // Subscribe to unread count
    const countSub = this.notificationService.unreadCount$.subscribe(
      count => this.unreadCount = count
    );

    this.subscriptions.push(notificationSub, countSub);
  }

  @HostListener('document:click', ['$event'])
  onClickOutside(event: Event) {
    const target = event.target as HTMLElement;
    if (!target.closest('.notification-container')) {
      this.showDropdown = false;
    }
  }

  private setupClickOutsideListener() {
    
  }

  toggleDropdown(event: Event) {
    event.stopPropagation();
    this.showDropdown = !this.showDropdown;

    if (this.showDropdown && this.unreadCount > 0) {
      this.markAllAsRead();
    }
  }
handleNotificationClick(notification: PatientSuivantNotification) {
    this.notificationService.markAsRead(notification.id).subscribe();
    
    if (notification.type === 'PATIENT_SUIVANT' && notification.dossierPatient) {
        const patientId = notification.dossierPatient.idPatient;
        
        if (!patientId) {
            console.error('❌ No patient ID found in notification:', notification);
            alert('Patient information is incomplete');
            return;
        }
        
        this.router.navigate(['/doctor/consultation'], {
            state: { 
                notificationId: notification.id,
                patientId: patientId,
                patientInfo: notification.dossierPatient,
                rendezVousId: notification.rendezVousId
            }
        });
        this.showDropdown = false;
    }
}

  markAsRead(notificationId: number) {
    this.notificationService.markAsRead(notificationId).subscribe();
  }

  markAllAsRead() {
    if (this.medecinId) {
      this.notificationService.markAllAsRead(this.medecinId).subscribe();
    }
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} min`;
    if (diffHours < 24) return `Il y a ${diffHours} h`;
    if (diffDays < 7) return `Il y a ${diffDays} j`;

    return date.toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  ngOnDestroy() {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.notificationService.clearNotifications();
  }
}