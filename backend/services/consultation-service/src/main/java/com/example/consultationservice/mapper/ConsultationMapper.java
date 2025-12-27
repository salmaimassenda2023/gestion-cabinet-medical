package com.example.consultationservice.mapper;

import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConsultationMapper {

    public ConsultationDTO toDTO(Consultation entity) {
        if (entity == null)
            return null;
        return ConsultationDTO.builder()
                .idConsultation(entity.getIdConsultation())
                .idPatient(entity.getIdPatient())
                .idMedecin(entity.getIdMedecin())
                .idRendezVous(entity.getIdRendezVous())
                .dateConsultation(entity.getDateConsultation())
                .diagnostic(entity.getDiagnostic())
                .examensCliniques(toExamenCliniqueDTOs(entity.getExamensCliniques()))
                .ordonnancesMedicaments(toOrdonnanceMedicamentDTOs(entity.getOrdonnancesMedicaments()))
                .ordonnancesExamens(toOrdonnanceExamenDTOs(entity.getOrdonnancesExamens()))
                .factures(toFactureDTOs(entity.getFactures()))
                .build();
    }

    public Consultation toEntity(ConsultationDTO dto) {
        if (dto == null)
            return null;
        Consultation consultation = Consultation.builder()
                .idConsultation(dto.getIdConsultation())
                .idPatient(dto.getIdPatient())
                .idMedecin(dto.getIdMedecin())
                .idRendezVous(dto.getIdRendezVous())
                .dateConsultation(dto.getDateConsultation())
                .diagnostic(dto.getDiagnostic())
                .build();

        // Handling lists is more complex bi-directionally, usually done in service
        // to set the parent reference, but simple mapping here is fine for now.
        return consultation;
    }

    private List<ExamenCliniqueDTO> toExamenCliniqueDTOs(List<ExamenClinique> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public ExamenCliniqueDTO toDTO(ExamenClinique entity) {
        return ExamenCliniqueDTO.builder()
                .idExamen(entity.getIdExamen())
                .typeExamen(entity.getTypeExamen())
                .valeur(entity.getValeur())
                .unite(entity.getUnite())
                .build();
    }

    private List<OrdonnanceMedicamentDTO> toOrdonnanceMedicamentDTOs(List<OrdonnanceMedicament> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OrdonnanceMedicamentDTO toDTO(OrdonnanceMedicament entity) {
        return OrdonnanceMedicamentDTO.builder()
                .idOrdonnance(entity.getIdOrdonnance())
                .dateCreation(entity.getDateCreation())
                .signee(entity.getSignee())
                .lignes(toLigneOrdonnanceMedicamentDTOs(entity.getLignes()))
                .build();
    }

    private List<LigneOrdonnanceMedicamentDTO> toLigneOrdonnanceMedicamentDTOs(List<LigneOrdonnanceMedicament> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public LigneOrdonnanceMedicamentDTO toDTO(LigneOrdonnanceMedicament entity) {
        return LigneOrdonnanceMedicamentDTO.builder()
                .idLigne(entity.getIdLigne())
                .idMedicament(entity.getIdMedicament())
                .nomMedicament(entity.getNomMedicament())
                .posologie(entity.getPosologie())
                .duree(entity.getDuree())
                .build();
    }

    private List<OrdonnanceExamenDTO> toOrdonnanceExamenDTOs(List<OrdonnanceExamen> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public OrdonnanceExamenDTO toDTO(OrdonnanceExamen entity) {
        return OrdonnanceExamenDTO.builder()
                .idOrdonnance(entity.getIdOrdonnance())
                .dateCreation(entity.getDateCreation())
                .signee(entity.getSignee())
                .lignes(toLigneOrdonnanceExamenDTOs(entity.getLignes()))
                .build();
    }

    private List<LigneOrdonnanceExamenDTO> toLigneOrdonnanceExamenDTOs(List<LigneOrdonnanceExamen> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public LigneOrdonnanceExamenDTO toDTO(LigneOrdonnanceExamen entity) {
        return LigneOrdonnanceExamenDTO.builder()
                .idLigne(entity.getIdLigne())
                .typeExamen(entity.getTypeExamen())
                .description(entity.getDescription())
                .build();
    }

    private List<FactureDTO> toFactureDTOs(List<Facture> list) {
        if (list == null)
            return Collections.emptyList();
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public FactureDTO toDTO(Facture entity) {
        return FactureDTO.builder()
                .idFacture(entity.getIdFacture())
                .dateFacture(entity.getDateFacture())
                .montant(entity.getMontant())
                .statut(entity.getStatut())
                .service(toDTO(entity.getService()))
                .build();
    }

    public ServiceConsultationDTO toDTO(ServiceConsultation entity) {
        if (entity == null)
            return null;
        return ServiceConsultationDTO.builder()
                .idService(entity.getIdService())
                .nomService(entity.getNomService())
                .prix(entity.getPrix())
                .build();
    }

    // Reverse mappings for lists is separate and complex, skipping for simplicity
    // in this initial pass
    // or adding them as needed in the controller/service logic manually or
    // expanding mapper later
}
