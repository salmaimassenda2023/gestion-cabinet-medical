package com.example.consultationservice.mapper;

import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


// ============ ConsultationMapper ============

@Component
public class ConsultationMapper {

    public Consultation toEntity(ConsultationCreateDTO dto) {
        if (dto == null) return null;

        return Consultation.builder()
                .idPatient(dto.getIdPatient())
                .diagnostic(dto.getDiagnostic())
                .build();
    }

    public ConsultationResponseDTO toDTO(Consultation entity) {
        if (entity == null) return null;

        return ConsultationResponseDTO.builder()
                .idConsultation(entity.getIdConsultation())
                .idPatient(entity.getIdPatient())
                .dateConsultation(entity.getDateConsultation())
                .diagnostic(entity.getDiagnostic())
                .build();
    }
}