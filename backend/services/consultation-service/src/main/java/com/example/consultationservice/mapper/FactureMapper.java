package com.example.consultationservice.mapper;

import com.example.consultationservice.dto.ConsultationServiceItemDTO;
import com.example.consultationservice.dto.FactureDTO;
import com.example.consultationservice.entity.Facture;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FactureMapper {

    public FactureDTO toDTO(Facture facture) {
        if (facture == null) {
            return null;
        }

        return FactureDTO.builder()
                .idFacture(facture.getIdFacture())
                .idConsultation(facture.getConsultation().getIdConsultation())
                .cabinetId(facture.getCabinetId())
                .montantTotal(facture.getMontantTotal())
                .dateFacture(facture.getDateFacture())
                .statut(facture.getStatut())
                .notes(facture.getNotes())
                // Map all services from the consultation
                .services(facture.getConsultation().getConsultationServices()
                        .stream()
                        .map(service -> ConsultationServiceItemDTO.builder()
                                .idService(service.getIdService())
                                .nomService(service.getNomService())
                                .prix(service.getPrix())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}