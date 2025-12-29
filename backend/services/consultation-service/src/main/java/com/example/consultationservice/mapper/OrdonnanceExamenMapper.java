package com.example.consultationservice.mapper;
import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrdonnanceExamenMapper {

    public OrdonnanceExamen toEntity(OrdonnanceExamenCreateDTO dto) {
        if (dto == null) return null;

        OrdonnanceExamen ordonnance = new OrdonnanceExamen();

        if (dto.getLignes() != null) {
            List<LigneOrdonnanceExamen> lignes = dto.getLignes().stream()
                    .map(this::ligneDtoToEntity)
                    .collect(Collectors.toList());
            ordonnance.setLignes(lignes);
        }

        return ordonnance;
    }

    public OrdonnanceExamenDTO toDTO(OrdonnanceExamen entity) {
        if (entity == null) return null;

        List<LigneOrdonnanceExamenDTO> lignesDTO = entity.getLignes() != null
                ? entity.getLignes().stream()
                .map(this::ligneEntityToDto)
                .collect(Collectors.toList())
                : Collections.emptyList();

        return OrdonnanceExamenDTO.builder()
                .idOrdonnance(entity.getIdOrdonnance())
                .dateCreation(entity.getDateCreation())
                .lignes(lignesDTO)
                .build();
    }

    private LigneOrdonnanceExamen ligneDtoToEntity(LigneOrdonnanceExamenCreateDTO dto) {
        return LigneOrdonnanceExamen.builder()
                .typeExamen(dto.getTypeExamen())
                .description(dto.getDescription())
                .build();
    }

    private LigneOrdonnanceExamenDTO ligneEntityToDto(LigneOrdonnanceExamen entity) {
        return LigneOrdonnanceExamenDTO.builder()
                .idLigne(entity.getIdLigne())
                .typeExamen(entity.getTypeExamen())
                .description(entity.getDescription())
                .build();
    }
}
