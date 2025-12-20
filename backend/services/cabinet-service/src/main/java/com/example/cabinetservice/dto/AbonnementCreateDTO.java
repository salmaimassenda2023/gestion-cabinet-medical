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
public class AbonnementCreateDTO {
    private TypePeriode typePeriode;
    private Double montant;
}
