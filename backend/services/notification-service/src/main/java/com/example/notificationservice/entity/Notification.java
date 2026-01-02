package com.example.notificationservice.entity;

// ============================================
// 2. entity/Notification.java
// ============================================

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotification;

    @Column(nullable = false)
    private Long idDestinataire;

    @Column(nullable = false)
    private Long idCabinet;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "dossier_patient_json", columnDefinition = "TEXT")
    private String dossierPatientJson;

    @Column(name = "abonnement_json", columnDefinition = "TEXT")
    private String abonnementJson;

    @Column(nullable = false)
    private Boolean lu = false;

    @Column(nullable = false, name = "date_envoi")
    private LocalDateTime dateEnvoi;

    @Column(name = "date_lecture")
    private LocalDateTime dateLecture;

    @PrePersist
    protected void onCreate() {
        if (dateEnvoi == null) {
            dateEnvoi = LocalDateTime.now();
        }
    }

    public void marquerCommeLue() {
        this.lu = true;
        this.dateLecture = LocalDateTime.now();
    }
}
