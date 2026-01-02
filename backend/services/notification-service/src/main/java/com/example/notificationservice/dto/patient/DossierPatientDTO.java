package com.example.notificationservice.dto.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierPatientDTO {
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
    // We might need a simplistic structure for documents if not available in notif
    // service,
    // or just ignore them for now to avoid compilation errors if DocumentMedicalDTO
    // is missing.
    // Let's assume we can skip documents or use Object for now to be safe, or just
    // list generic.
    // private List<DocumentMedicalDTO> documents;
}
