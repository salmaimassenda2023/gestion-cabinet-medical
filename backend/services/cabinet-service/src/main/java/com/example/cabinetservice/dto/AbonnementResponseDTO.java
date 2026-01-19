package com.example.cabinetservice.dto;

import com.example.cabinetservice.enums.AbonnementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonnementResponseDTO {
    private Long idAbonnement;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private AbonnementStatus statut;
    private Double montant;
    private String typePeriode;
    private String cabinetNom;
    private String cabinetLogo;
}