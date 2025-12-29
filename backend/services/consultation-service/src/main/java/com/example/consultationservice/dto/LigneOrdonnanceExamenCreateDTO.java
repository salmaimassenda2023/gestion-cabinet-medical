package com.example.consultationservice.dto;

import com.example.consultationservice.enums.TypeExamenSupplementaire;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdonnanceExamenCreateDTO {
    @NotNull(message = "Le type d'examen est obligatoire")
    private TypeExamenSupplementaire typeExamen;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;
}