package com.example.notificationservice.dto;

import com.example.notificationservice.dto.abonnement_cabinet.AbonnementExpirationNotificationDTO;
import com.example.notificationservice.dto.patient.DossierPatientDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private Long userId;
    private Long adminId; 
    private String type;
    private String titre;
    private DossierPatientDTO dossierPatient;
    private AbonnementExpirationNotificationDTO abonnement;
    private Long cabinetId;

    // Helper to get the target ID regardless of field used
    public Long getTargetId() {
        return userId != null ? userId : adminId;
    }
}
