import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, interval, Subscription, of, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { tap, catchError, switchMap, startWith } from 'rxjs/operators';

export interface DossierPatient {
    idPatient: number;
    nomComplet: string;
    age?: number;
    telephone?: string;
    motif?: string;
    ordrePassage?: number;
    idCabinet?: number;
    sexe?: string;
    dateNaissance?: string;
    nom?: string;
    prenom?: string;
    email?: string;
    antecedentsMedicaux?: string;
    antecedentsChirurgicaux?: string;
    allergies?: string;
    groupeSanguin?: string;
    remarques?: string;
}

export interface NotificationRequest {
    type: string;
    targetId: number;
    titre: string;
    dossierPatient?: DossierPatient;
    rendezVousId?: number;
    userId?: number;
    adminId?: number;
}

export interface PatientSuivantNotification {
    id: number;
    type: string;
    titre: string;
    message?: string;
    lu: boolean;
    dateEnvoi: string;
    dossierPatient?: DossierPatient;
    rendezVousId?: number;
}

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private apiUrl = `${environment.apiUrl}/api/notification`;

    private notificationsSubject = new BehaviorSubject<PatientSuivantNotification[]>([]);
    private unreadCountSubject = new BehaviorSubject<number>(0);
    private pollingSubscription?: Subscription;
    private currentUserId?: number;

    notifications$ = this.notificationsSubject.asObservable();
    unreadCount$ = this.unreadCountSubject.asObservable();

    constructor(private http: HttpClient) { }

    /**
     * Initialize notification system for a user
     */
    initializeForUser(userId: number): void {
        this.currentUserId = userId;
        this.startPolling(userId);
    }

    /**
     * Start polling for notifications (every 5 seconds)
     */
    private startPolling(userId: number): void {
        if (this.pollingSubscription) {
            this.pollingSubscription.unsubscribe();
        }

        this.pollingSubscription = interval(5000).pipe(
            startWith(0),
            switchMap(() => this.getPatientSuivantNotifications(userId)),
            catchError(err => {
                console.error('❌ Polling error:', err);
                return of([]);
            })
        ).subscribe({
            next: (notifications) => {
                this.updateNotifications(notifications);
            }
        });
    }

    /**
     * Update notifications with proper merging
     */
    private updateNotifications(newNotifications: PatientSuivantNotification[]): void {
        const currentNotifications = this.notificationsSubject.value;

        // Create a map of existing notifications by ID
        const notificationMap = new Map<number, PatientSuivantNotification>();
        currentNotifications.forEach(n => notificationMap.set(n.id, n));

        // Update or add new notifications
        newNotifications.forEach(newNotif => {
            const existing = notificationMap.get(newNotif.id);
            if (existing) {
                // Update read status if changed on server
                if (existing.lu !== newNotif.lu) {
                    existing.lu = newNotif.lu;
                }
            } else {
                // Add new notification
                notificationMap.set(newNotif.id, newNotif);
            }
        });

        // Convert back to array and sort by date (newest first)
        const updatedNotifications = Array.from(notificationMap.values())
            .sort((a, b) => new Date(b.dateEnvoi).getTime() - new Date(a.dateEnvoi).getTime());

        this.notificationsSubject.next(updatedNotifications);

        // Update unread count
        const unreadCount = updatedNotifications.filter(n => !n.lu).length;
        this.unreadCountSubject.next(unreadCount);
    }
getNotificationById(notificationId: number): Observable<PatientSuivantNotification> {
    return this.http.get<PatientSuivantNotification>(`${this.apiUrl}/${notificationId}`);
}

    /**
     * Send a "patient suivant" notification to the doctor
     */
sendPatientSuivantNotification(
    medecinId: number, 
    patientInfo: any,
    rendezVousId: number,
    patientId?: number,
    cabinetId?: number
): Observable<void> {
    
    const actualPatientId = patientId || patientInfo.idPatient || patientInfo.patientId || patientInfo.id;
    
    const request = {
        userId: medecinId,
        type: 'PATIENT_SUIVANT',
        titre: `Patient Suivant - N°${actualPatientId} : ${patientInfo.nom} ${patientInfo.prenom}`,
        dossierPatient: {
            ...patientInfo,
            idPatient: actualPatientId,
            patientId: actualPatientId
        },
        cabinetId: cabinetId || 0
    };

    console.log('📤 Sending notification request:', request);
    
    return this.http.post<void>(this.apiUrl, request).pipe(
        tap(() => {
            console.log('✅ Notification sent successfully');
            const newNotification: PatientSuivantNotification = {
                id: Date.now(),
                type: 'PATIENT_SUIVANT',
                titre: request.titre,
                lu: false,
                dateEnvoi: new Date().toISOString(),
                dossierPatient: request.dossierPatient,
                rendezVousId: rendezVousId
            };
            
            const current = this.notificationsSubject.value;
            this.notificationsSubject.next([newNotification, ...current]);
            this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
        }),
        catchError(err => {
            console.error('❌ Error sending notification:', err);
            const newNotification: PatientSuivantNotification = {
                id: Date.now(),
                type: 'PATIENT_SUIVANT',
                titre: `Patient Suivant - N°${actualPatientId} : ${patientInfo.nom} ${patientInfo.prenom}`,
                lu: false,
                dateEnvoi: new Date().toISOString(),
                dossierPatient: request.dossierPatient,
                rendezVousId: rendezVousId
            };
            
            const current = this.notificationsSubject.value;
            this.notificationsSubject.next([newNotification, ...current]);
            this.unreadCountSubject.next(this.unreadCountSubject.value + 1);
            
            return throwError(() => err);
        })
    );
}
    /**
     * Get all PATIENT_SUIVANT notifications for a doctor
     */
    getPatientSuivantNotifications(medecinId: number): Observable<PatientSuivantNotification[]> {
        return this.http.get<PatientSuivantNotification[]>(`${this.apiUrl}/patient-suivant/${medecinId}`).pipe(
            tap(notifications => {
                console.log('📥 Received notifications:', notifications.length);
            })
        );
    }

    /**
     * Get next patient notification (most recent unread)
     */
    getNextPatientNotification(medecinId: number): Observable<PatientSuivantNotification | null> {
        return this.getPatientSuivantNotifications(medecinId).pipe(
            switchMap(notifications => {
                const nextPatientNotification = notifications
                    .filter(n => n.type === 'PATIENT_SUIVANT' && !n.lu)
                    .sort((a, b) => new Date(b.dateEnvoi).getTime() - new Date(a.dateEnvoi).getTime())[0];

                return of(nextPatientNotification || null);
            })
        );
    }

    /**
     * Mark a notification as read
     */
    markAsRead(notificationId: number): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${notificationId}/read`, {}).pipe(
            tap(() => {
                // Update local state immediately
                const notifications = this.notificationsSubject.value.map(n =>
                    n.id === notificationId ? { ...n, lu: true } : n
                );
                this.notificationsSubject.next(notifications);

                const unreadCount = notifications.filter(n => !n.lu).length;
                this.unreadCountSubject.next(unreadCount);
            })
        );
    }

    /**
     * Mark all notifications as read for a user
     */
    markAllAsRead(userId: number): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/user/${userId}/read-all`, {}).pipe(
            tap(() => {
                // Update local state immediately
                const notifications = this.notificationsSubject.value.map(n => ({ ...n, lu: true }));
                this.notificationsSubject.next(notifications);
                this.unreadCountSubject.next(0);
            })
        );
    }

    /**
     * Get count of unread notifications
     */
    countUnread(userId: number): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/user/${userId}/unread-count`);
    }

    /**
     * Clear local notifications (for logout)
     */
    clearNotifications(): void {
        this.notificationsSubject.next([]);
        this.unreadCountSubject.next(0);

        if (this.pollingSubscription) {
            this.pollingSubscription.unsubscribe();
            this.pollingSubscription = undefined;
        }
    }

    /**
     * Cleanup on service destroy
     */
    ngOnDestroy(): void {
        this.clearNotifications();
    }
}