package com.example.cabinetservice.mapper;

import com.example.cabinetservice.dto.*;
import com.example.cabinetservice.entity.AbonnementCabinet;
import com.example.cabinetservice.entity.Cabinet;
import com.example.cabinetservice.entity.PaiementAbonnement;
import com.example.cabinetservice.entity.ServiceConsultation;
import com.example.cabinetservice.enums.AbonnementStatus;
import org.springframework.stereotype.Component;

@Component
public class CabinetMapper {

    public Cabinet toEntity(CabinetCreateDTO dto) {
        if (dto == null)
            return null;

        return Cabinet.builder()
                .nom(dto.getNom())
                .specialite(dto.getSpecialite())
                .adresse(dto.getAdresse())
                .tel(dto.getTel())
                .logo(dto.getLogo())
                .maxPatientsJour(dto.getMaxPatientsJour())
                .dureeConsultation(dto.getDureeConsultation())
                .medecinId(dto.getMedecinId())
                .actif(true) 
                .build();
    }

    public CabinetResponseDTO toDto(Cabinet cabinet) {
        if (cabinet == null)
            return null;

        return CabinetResponseDTO.builder()
                .id(cabinet.getId())
                .nom(cabinet.getNom())
                .specialite(cabinet.getSpecialite())
                .adresse(cabinet.getAdresse())
                .tel(cabinet.getTel())
                .logo(cabinet.getLogo())
                .maxPatientsJour(cabinet.getMaxPatientsJour())
                .dureeConsultation(cabinet.getDureeConsultation())
                .actif(cabinet.getActif())
                .abonnement(toDto(cabinet.getAbonnement()))
                .services(null)
                .build();
    }

    public AbonnementCabinet toEntity(AbonnementCreateDTO dto) {
        if (dto == null)
            return null;

        return AbonnementCabinet.builder()
                .montant(dto.getMontant())
                .typePeriode(dto.getTypePeriode())
                .statut(AbonnementStatus.ACTIF) 
                .build();
    }

    public AbonnementResponseDTO toDto(AbonnementCabinet entity) {
        if (entity == null)
            return null;

        return AbonnementResponseDTO.builder()
                .idAbonnement(entity.getIdAbonnement())
                .dateDebut(entity.getDateDebut())
                .dateFin(entity.getDateFin())
                .statut(entity.getStatut())
                .montant(entity.getMontant())
                .typePeriode(entity.getTypePeriode().name())
                .build();
    }

    public ServiceConsultation toEntity(ServiceConsultationDTO dto) {
        if (dto == null)
            return null;

        return ServiceConsultation.builder()
                .nomService(dto.getNomService())
                .description(dto.getDescription())
                .prix(dto.getPrix())
                .obligatoire(dto.getObligatoire() != null ? dto.getObligatoire() : false)
                .build();
    }

    public ServiceConsultationDTO toDto(ServiceConsultation entity) {
        if (entity == null)
            return null;

        return ServiceConsultationDTO.builder()
                .idService(entity.getIdService())
                .nomService(entity.getNomService())
                .description(entity.getDescription())
                .prix(entity.getPrix())
                .obligatoire(entity.getObligatoire())
                .build();
    }

    public PaiementResponseDTO toDto(PaiementAbonnement entity) {
        if (entity == null)
            return null;

        return PaiementResponseDTO.builder()
                .idPaiement(entity.getIdPaiement())
                .datePaiement(entity.getDatePaiement())
                .montant(entity.getMontant())
                .statut(entity.getStatut())
                .build();
    }
}
