package com.example.notificationservice.dto.abonnement_cabinet;

import com.example.notificationservice.dto.BaseNotificationDTO;
import lombok.*;
// Réponse pour le frontend (Admin)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class AbonnementExpirationResponseDTO extends BaseNotificationDTO {
    private AbonnementInfoDTO abonnementInfo;

    @Builder
    public AbonnementExpirationResponseDTO(Long id, String type, String titre, String message,
                                           Boolean lu, String priorite, String dateEnvoi,
                                           AbonnementInfoDTO abonnementInfo) {
        super(id, type, titre, message, lu, dateEnvoi);
        this.abonnementInfo = abonnementInfo;
    }
}
