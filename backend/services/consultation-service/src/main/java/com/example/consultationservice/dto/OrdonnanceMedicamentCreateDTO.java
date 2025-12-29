package com.example.consultationservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdonnanceMedicamentCreateDTO {
    @NotEmpty(message = "L'ordonnance doit contenir au moins un médicament")
    private List<LigneOrdonnanceMedicamentCreateDTO> lignes;
}