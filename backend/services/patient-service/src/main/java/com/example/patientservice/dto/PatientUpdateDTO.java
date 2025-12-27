package com.example.patientservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateDTO {
    private String nom;
    private String prenom;
    private String telephone;
    private String email;
    private String adresse;
    private String typeMutuelle;
    private String numeroMutuelle;
}
