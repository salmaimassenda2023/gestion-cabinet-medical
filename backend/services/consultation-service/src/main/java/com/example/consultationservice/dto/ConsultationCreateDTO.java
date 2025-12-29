package com.example.consultationservice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// ============ Consultation DTOs ============

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationCreateDTO {
    @NotNull(message = "L'ID du patient est obligatoire")
    private Long idPatient;
    private Long idCabinet;

    @Size(max = 2000, message = "Le diagnostic ne doit pas dépasser 2000 caractères")
    private String diagnostic;
    private List<ConsultationServiceItemDTO> services;


}