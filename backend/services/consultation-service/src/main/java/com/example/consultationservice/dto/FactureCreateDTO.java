package com.example.consultationservice.dto;

import com.example.consultationservice.enums.TypeExamenClinique;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureCreateDTO {
    private String statut;

    private String notes;

}