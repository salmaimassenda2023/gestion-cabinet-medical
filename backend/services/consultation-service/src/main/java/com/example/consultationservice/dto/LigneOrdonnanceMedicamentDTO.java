package com.example.consultationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdonnanceMedicamentDTO {
    private Long idLigne;
    private String idMedicament;
    private String nomMedicament;
    private String posologie;
    private String duree;
}
