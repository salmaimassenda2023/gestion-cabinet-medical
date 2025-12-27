package com.example.consultationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationDTO {
    private Long idConsultation;
    private Long idPatient;
    private Long idMedecin;
    private Long idRendezVous;
    private Date dateConsultation;
    private String diagnostic;
    private List<ExamenCliniqueDTO> examensCliniques;
    private List<OrdonnanceMedicamentDTO> ordonnancesMedicaments;
    private List<OrdonnanceExamenDTO> ordonnancesExamens;
    private List<FactureDTO> factures;
}
