package com.example.consultationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {
    private Long idFacture;
    private Date dateFacture;
    private Double montant;
    private String statut;
    private ServiceConsultationDTO service;
}
