package com.example.cabinetservice.dto;



import com.example.cabinetservice.enums.TypePeriode;
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
    private String logo;
    private Long medecinId;
    private AbonnementCreateDTO abonnement;
    private ServiceConsultationDTO serviceConsultationGenerale;
}
