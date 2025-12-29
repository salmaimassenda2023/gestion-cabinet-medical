package com.example.consultationservice.mapper;
import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class OrdonnanceMedicamentMapper {

    public OrdonnanceMedicament toEntity(OrdonnanceMedicamentCreateDTO dto) {
        if (dto == null) return null;

        OrdonnanceMedicament ordonnance = new OrdonnanceMedicament();

        if (dto.getLignes() != null) {
            List<LigneOrdonnanceMedicament> lignes = dto.getLignes().stream()
                    .map(this::ligneDtoToEntity)
                    .collect(Collectors.toList());
            ordonnance.setLignes(lignes);
        }

        return ordonnance;
    }

    public OrdonnanceMedicamentDTO toDTO(OrdonnanceMedicament entity) {
        if (entity == null) return null;

        List<LigneOrdonnanceMedicamentDTO> lignesDTO = entity.getLignes() != null
                ? entity.getLignes().stream()
                .map(this::ligneEntityToDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return OrdonnanceMedicamentDTO.builder()
                .idOrdonnance(entity.getIdOrdonnance())
                .dateCreation(entity.getDateCreation())
                .lignes(lignesDTO)
                .build();
    }

    private LigneOrdonnanceMedicament ligneDtoToEntity(LigneOrdonnanceMedicamentCreateDTO dto) {
        return LigneOrdonnanceMedicament.builder()
                .idMedicament(dto.getIdMedicament())
                .nomMedicament(dto.getNomMedicament())
                .posologie(dto.getPosologie())
                .duree(dto.getDuree())
                .build();
    }

    private LigneOrdonnanceMedicamentDTO ligneEntityToDto(LigneOrdonnanceMedicament entity) {
        return LigneOrdonnanceMedicamentDTO.builder()
                .idLigne(entity.getIdLigne())
                .idMedicament(entity.getIdMedicament())
                .nomMedicament(entity.getNomMedicament())
                .posologie(entity.getPosologie())
                .duree(entity.getDuree())
                .build();
    }
}