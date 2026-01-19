package com.example.consultationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureCreateDTO {
    private String statut;

    private String notes;

    private List<Long> serviceIds;
    private Double montantTotal;
}