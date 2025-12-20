package com.example.cabinetservice.dto;

import com.example.cabinetservice.enums.PaiementStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementResponseDTO {
    private Long idPaiement;
    private LocalDateTime datePaiement;
    private Double montant;
    private PaiementStatus statut;
}
