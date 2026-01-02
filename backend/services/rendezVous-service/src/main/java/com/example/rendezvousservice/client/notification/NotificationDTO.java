package com.example.rendezvousservice.client.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long userId; // ID du médecin
    private String type; // "PATIENT_SUIVANT"
    private String titre; // "Patient Suivant"
    private NotificationDossierDTO dossierPatient; // Dossier complet pour le médecin
}
