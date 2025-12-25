package com.example.patientservice.mapper;


import com.example.patientservice.dto.*;
import com.example.patientservice.entity.DocumentMedical;
import com.example.patientservice.entity.DossierMedical;
import com.example.patientservice.entity.Patient;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PatientMapper {

    public Patient toEntity(PatientCreateDTO dto) {
        return Patient.builder()
                .cin(dto.getCin())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .dateNaissance(dto.getDateNaissance())
                .sexe(dto.getSexe())
                .telephone(dto.getTelephone())
                .email(dto.getEmail())
                .adresse(dto.getAdresse())
                .typeMutuelle(dto.getTypeMutuelle())
                .numeroMutuelle(dto.getNumeroMutuelle())
                .idCabinet(dto.getIdCabinet())
                .build();
    }

    public PatientResponseDTO toDto(Patient patient) {
        PatientResponseDTO dto = PatientResponseDTO.builder()
                .id(patient.getId())
                .cin(patient.getCin())
                .nom(patient.getNom())
                .prenom(patient.getPrenom())
                .dateNaissance(patient.getDateNaissance())
                .sexe(patient.getSexe())
                .telephone(patient.getTelephone())
                .email(patient.getEmail())
                .adresse(patient.getAdresse())
                .typeMutuelle(patient.getTypeMutuelle())
                .numeroMutuelle(patient.getNumeroMutuelle())
                .idCabinet(patient.getIdCabinet())
                .createdAt(patient.getCreatedAt())
                .build();

        if (patient.getDossierMedical() != null) {
            dto.setDossierMedical(toDto(patient.getDossierMedical()));
        }

        return dto;
    }

    public DossierMedicalDTO toDto(DossierMedical dossier) {
        List<DocumentMedicalDTO> documentDTOs = dossier.getDocuments().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return DossierMedicalDTO.builder()
                .idDossier(dossier.getId())
                .antecedentsMedicaux(dossier.getAntecedentsMedicaux())
                .antecedentsChirurgicaux(dossier.getAntecedentsChirurgicaux())
                .allergies(dossier.getAllergies())
                .groupeSanguin(dossier.getGroupeSanguin())
                .remarques(dossier.getRemarques())
                .dateCreation(dossier.getDateCreation())
                .documents(documentDTOs)
                .build();
    }

    public DocumentMedicalDTO toDto(DocumentMedical document) {
        return DocumentMedicalDTO.builder()
                .idDocument(document.getIdDocument())
                .type(document.getType())
                .nom(document.getNom())
                .url(document.getUrl())
                .tailleOctets(document.getTailleOctets())
                .build();
    }
}
