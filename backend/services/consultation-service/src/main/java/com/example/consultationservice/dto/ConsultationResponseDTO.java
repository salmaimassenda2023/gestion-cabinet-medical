package com.example.consultationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponseDTO {
    private Long idConsultation;
    private Long idPatient;
    private Long idCabinet;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Casablanca")
    private Date dateConsultation;

    private String diagnostic;
    private List<ExamenCliniqueDTO> examensCliniques;
    private List<OrdonnanceMedicamentDTO> ordonnancesMedicaments;
    private List<OrdonnanceExamenDTO> ordonnancesExamens;
    private List<FactureDTO> factures;
    private List<ConsultationServiceItemDTO> services;
    private Double montantTotal;
}