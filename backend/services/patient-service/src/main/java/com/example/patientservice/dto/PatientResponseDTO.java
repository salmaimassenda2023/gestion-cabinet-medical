package com.example.patientservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {
    private Long id;
    private String cin;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String sexe;
    private String telephone;
    private String email;
    private String adresse;
    private String typeMutuelle;
    private String numeroMutuelle;
    private Long idCabinet;
    private Boolean actif;
    private LocalDateTime createdAt;
    private DossierMedicalDTO dossierMedical;
}
