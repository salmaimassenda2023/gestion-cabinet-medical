package com.example.cabinetservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardAdminDTO {

    private Long nombreTotalCabinets;
    private Long nombreCabinetsActifs;
    private Long nombreCabinetsInactifs;
    private BigDecimal revenusGlobaux;
    private List<AbonnementResponseDTO> abonnementsExpirantBientot;
}
