package com.example.cabinetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetCreateDTO {
    private String nom;
    private String specialite;
    private String adresse;
    private String tel;
    private Integer maxPatientsJour;
    private Integer dureeConsultation;
    private String logo;
    private Long medecinId;
    private AbonnementCreateDTO abonnement;
    private ServiceConsultationDTO serviceConsultationGenerale;
}
