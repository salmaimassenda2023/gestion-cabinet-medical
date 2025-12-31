package com.example.rendezvousservice.client.patient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Fallback pour le client Patient en cas d'indisponibilité du service.
 */
@Component
@Slf4j
public class PatientClientFallback implements PatientClient {

    @Override
    public PatientInfoDTO getPatientInfo(Long patientId) {
        log.warn("️ Fallback: Service Patient indisponible pour patient ID: {}", patientId);

        return PatientInfoDTO.builder()
                .id(patientId)
                .nom("Service temporairement indisponible")
                .prenom("")
                .build();
    }

    @Override
    public DossierMedicalDTO getDossierMedical(Long patientId) {
        log.warn("⚠️ Fallback: Dossier médical indisponible pour patient ID: {}", patientId);

        return DossierMedicalDTO.builder()
                .idDossier(null)
                .antecedentsMedicaux("Service temporairement indisponible")
                .antecedentsChirurgicaux("Service temporairement indisponible")
                .allergies("Service temporairement indisponible")
                .groupeSanguin("N/A")
                .remarques("Dossier médical temporairement indisponible")
                .dateCreation(LocalDateTime.now())
                .documents(Collections.emptyList())
                .build();
    }
}
