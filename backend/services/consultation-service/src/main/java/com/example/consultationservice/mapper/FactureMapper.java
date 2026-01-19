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

        FactureDTO dto = FactureDTO.builder()
                .idFacture(facture.getIdFacture())
                .idConsultation(
                        facture.getConsultation() != null ? facture.getConsultation().getIdConsultation() : null)
                .cabinetId(facture.getCabinetId())
                .montantTotal(facture.getMontantTotal())
                .dateFacture(facture.getDateFacture())
                .statut(facture.getStatut())
                .notes(facture.getNotes())
                .build();

        // Map consultation services if available
        if (facture.getConsultation() != null &&
                facture.getConsultation().getConsultationServices() != null) {
            dto.setServices(
                    facture.getConsultation().getConsultationServices().stream()
                            .map(this::toServiceItemDTO)
                            .collect(Collectors.toList()));

            // Add consultation details
            dto.setIdPatient(facture.getConsultation().getIdPatient());
            dto.setDateConsultation(facture.getConsultation().getDateConsultation());
        }

        return dto;
    }

    /**
     * Enhanced version that includes patient name
     */
    public FactureDTO toDTOWithPatientName(Facture facture, String patientName) {
        FactureDTO dto = toDTO(facture);
        if (dto != null) {
            dto.setPatientName(patientName);
        }
        return dto;
    }

    private ConsultationServiceItemDTO toServiceItemDTO(
            com.example.consultationservice.entity.ConsultationServiceItem item) {
        if (item == null) {
            return null;
        }

        return ConsultationServiceItemDTO.builder()
                .idService(item.getIdService())
                .nomService(item.getNomService())
                .prix(item.getPrix())
                .build();
    }

    public Facture toEntity(FactureDTO dto) {
        if (dto == null) {
            return null;
        }

        return Facture.builder()
                .idFacture(dto.getIdFacture())
                .cabinetId(dto.getCabinetId())
                .montantTotal(dto.getMontantTotal())
                .dateFacture(dto.getDateFacture())
                .statut(dto.getStatut())
                .notes(dto.getNotes())
                .build();
    }
}