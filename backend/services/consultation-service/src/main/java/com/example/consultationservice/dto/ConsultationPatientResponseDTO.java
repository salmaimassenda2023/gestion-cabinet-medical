package com.example.consultationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ConsultationPatientResponseDTO {
    private Long idPatient;
    private String cin;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Africa/Casablanca")
    private Date dateNaissance;

    private String sexe;
}