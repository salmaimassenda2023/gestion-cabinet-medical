package com.example.cabinetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetResponseDTO {
    private Long id;
    private String nom;
    private String specialite;
    private String adresse;
    private String tel;
    private String logo;
    private Integer maxPatientsJour;
    private Integer dureeConsultation;
    private Boolean actif;
    private AbonnementResponseDTO abonnement;
    private List<ServiceConsultationDTO> services;
}
