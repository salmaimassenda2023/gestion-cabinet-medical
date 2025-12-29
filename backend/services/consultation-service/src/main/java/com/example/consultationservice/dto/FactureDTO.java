package com.example.consultationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {

    private Long idFacture;

    private Long idConsultation;

    private Long cabinetId;

    private Double montantTotal;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Casablanca")
    private Date dateFacture;

    private String statut;  // EN_ATTENTE, PAYEE, ANNULEE

    private String notes;

    // List of all services included in this invoice
    private List<ConsultationServiceItemDTO> services;
}