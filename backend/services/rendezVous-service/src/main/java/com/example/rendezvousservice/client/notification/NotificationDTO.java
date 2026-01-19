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
    private Long userId; 
    private String type; 
    private String titre; 
    private NotificationDossierDTO dossierPatient; 
}
