package com.example.notificationservice.dto.abonnement_cabinet;

import lombok.*;
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class AbonnementInfoDTO {
    private Long cabinetId;
    private String nomCabinet;
    private String dateExpiration;
    private Integer joursRestants;
    private Double montant;
}
