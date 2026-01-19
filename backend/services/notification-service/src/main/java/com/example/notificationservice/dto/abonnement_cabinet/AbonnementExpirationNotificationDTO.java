package com.example.notificationservice.dto.abonnement_cabinet;

import lombok.*;

// Notification reçue du service Cabinet
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class AbonnementExpirationNotificationDTO {
    private Long adminId;             
    private Long cabinetId;           
    private String nomCabinet;        
    private String dateExpiration;    
    private Integer joursRestants;    
    private Double montant;           
}
