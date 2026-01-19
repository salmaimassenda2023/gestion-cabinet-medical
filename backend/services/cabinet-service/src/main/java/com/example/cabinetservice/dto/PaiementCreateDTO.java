package com.example.cabinetservice.dto;


import com.example.cabinetservice.enums.ModePaiement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementCreateDTO {

    @NotNull(message = "L'ID de l'abonnement est obligatoire")
    private Long abonnementId;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    private BigDecimal montant;

    @NotNull(message = "Le mode de paiement est obligatoire")
    private ModePaiement modePaiement;

    private String referenceTransaction;
}
