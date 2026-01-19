package com.example.patientservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateDTO {
    private String cin;
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
    private String typeMutuelle;
    private String numeroMutuelle;
    private LocalDate dateNaissance;
    private String sexe;
}
