package com.example.consultationservice.dto;

import com.example.consultationservice.enums.TypeExamenSupplementaire;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdonnanceExamenDTO {
    private Long idLigne;
    private TypeExamenSupplementaire typeExamen;
    private String description;
}