package com.example.patientservice.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientCreateDTO {

    @NotBlank(message = "Le CIN est obligatoire")
    @Size(max = 20, message = "Le CIN ne peut pas dépasser 20 caractères")
    private String cin;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotBlank(message = "Le sexe est obligatoire")
    @Pattern(regexp = "^[MF]$", message = "Le sexe doit être M ou F")
    private String sexe;

    @Pattern(regexp = "^\\+?[0-9]{10,20}$", message = "Numéro de téléphone invalide")
    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    private String adresse;

    private String typeMutuelle;

    private String numeroMutuelle;

    @NotNull(message = "L'ID du cabinet est obligatoire")
    private Long idCabinet;
}
