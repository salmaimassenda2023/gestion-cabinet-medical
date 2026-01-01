package com.example.rendezvousservice.client.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDossierDTO {
    // Patient Info
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    // Medical Info
    private Long idDossier;
    private String antecedentsMedicaux;
    private String antecedentsChirurgicaux;
    private String allergies;
    private String groupeSanguin;
    private String remarques;
    private LocalDateTime dateCreation;
}
