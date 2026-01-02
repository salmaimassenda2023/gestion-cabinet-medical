package com.example.notificationservice.dto.patient;


import com.example.notificationservice.dto.BaseNotificationDTO;
import com.example.notificationservice.dto.patient.DossierPatientDTO;
import lombok.*;
// Réponse pour le frontend (Médecin)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PatientSuivantResponseDTO extends BaseNotificationDTO {
    private DossierPatientDTO dossierPatient;

    @Builder
    public PatientSuivantResponseDTO(Long id, String type, String titre, String message,
                                     Boolean lu, String dateEnvoi,
                                     DossierPatientDTO dossierPatient) {
        super(id, type, titre, message, lu, dateEnvoi);
        this.dossierPatient = dossierPatient;
    }
}
