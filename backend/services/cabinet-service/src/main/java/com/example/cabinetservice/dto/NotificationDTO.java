package com.example.cabinetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private Long adminId;
    private String type; 
    private String titre;
    private AbonnementExpirationDTO abonnement;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AbonnementExpirationDTO {
        private Long cabinetId;
        private String nomCabinet;
        private LocalDate dateExpiration;
        private int joursRestants;
        private Double montant;
        private Long adminId;
    }
}
