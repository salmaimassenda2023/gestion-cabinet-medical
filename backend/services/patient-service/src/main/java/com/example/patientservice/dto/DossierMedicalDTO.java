package com.example.patientservice.dto;


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
public class DossierMedicalDTO {
    private Long idDossier;
    private String antecedentsMedicaux;
    private String antecedentsChirurgicaux;
    private String allergies;
    private String groupeSanguin;
    private String remarques;
    private LocalDateTime dateCreation;
    private List<DocumentMedicalDTO> documents;
}
