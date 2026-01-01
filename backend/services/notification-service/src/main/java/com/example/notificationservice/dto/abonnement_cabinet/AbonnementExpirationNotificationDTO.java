package com.example.notificationservice.dto.abonnement_cabinet;

import lombok.*;

// Notification reçue du service Cabinet
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class AbonnementExpirationNotificationDTO {
    private Long adminId;             // ID de l'admin destinataire
    private Long cabinetId;           // ID du cabinet
    private String nomCabinet;        // Nom du cabinet
    private String dateExpiration;    // Date d'expiration
    private Integer joursRestants;    // Jours restants
    private Double montant;           // Montant de l'abonnement
}
