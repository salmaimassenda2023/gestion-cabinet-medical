package com.example.consultationservice.mapper;
import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import org.springframework.stereotype.Component;

@Component
public class ExamenCliniqueMapper {

    public ExamenClinique toEntity(ExamenCliniqueCreateDTO dto) {
        if (dto == null) return null;

        return ExamenClinique.builder()
                .typeExamen(dto.getTypeExamen())
                .valeur(dto.getValeur())
                .unite(dto.getUnite())
                .build();
    }

    public ExamenCliniqueDTO toDTO(ExamenClinique entity) {
        if (entity == null) return null;

        return ExamenCliniqueDTO.builder()
                .idExamen(entity.getIdExamen())
                .typeExamen(entity.getTypeExamen())
                .valeur(entity.getValeur())
                .unite(entity.getUnite())
                .build();
    }
}