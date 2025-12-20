package com.example.cabinetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConsultationDTO {
    private Long idService;
    private String nomService;
    private String description;
    private Double prix;
    private Boolean obligatoire;
}
