package com.example.consultationservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdonnanceMedicamentCreateDTO {
    @NotBlank(message = "L'ID du médicament est obligatoire")
    private String idMedicament;

    @NotBlank(message = "Le nom du médicament est obligatoire")
    private String nomMedicament;

    @NotBlank(message = "La posologie est obligatoire")
    private String posologie;

    @NotBlank(message = "La durée est obligatoire")
    private String duree;
}