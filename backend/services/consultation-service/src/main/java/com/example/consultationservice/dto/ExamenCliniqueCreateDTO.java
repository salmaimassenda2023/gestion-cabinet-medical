package com.example.consultationservice.dto;

import com.example.consultationservice.enums.TypeExamenClinique;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenCliniqueCreateDTO {
    @NotNull(message = "Le type d'examen est obligatoire")
    private TypeExamenClinique typeExamen;

    @NotBlank(message = "La valeur est obligatoire")
    private String valeur;

    @NotBlank(message = "L'unité est obligatoire")
    private String unite;
}