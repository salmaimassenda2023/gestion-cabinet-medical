package com.example.cabinetservice.scheduler;

import com.example.cabinetservice.dto.NotificationDTO;
import com.example.cabinetservice.entity.AbonnementCabinet;
import com.example.cabinetservice.enums.AbonnementStatus;
import com.example.cabinetservice.repository.AbonnementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CabinetScheduledTasks {

    private final AbonnementRepository abonnementRepository;
    private final com.example.cabinetservice.client.NotificationClient notificationClient;

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void checkExpiringAbonnements() {
        log.info("Checking for expiring abonnements...");
        // Compute date in 7 days
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);

        // Use the repository method to find expiring subscriptions
        List<AbonnementCabinet> expiring = abonnementRepository.findExpiringAbonnements(sevenDaysFromNow,
                AbonnementStatus.ACTIF);

        if (!expiring.isEmpty()) {
            log.info("Found {} expiring abonnements.", expiring.size());
            expiring.forEach(abo -> {
                try {
                    
                    Long adminId = abo.getCabinet().getMedecinId();

                    NotificationDTO.AbonnementExpirationDTO aboDto = NotificationDTO.AbonnementExpirationDTO.builder()
                            .cabinetId(abo.getCabinet().getId())
                            .nomCabinet(abo.getCabinet().getNom())
                            .dateExpiration(abo.getDateFin().toLocalDate())
                            .joursRestants(7)
                            .montant(abo.getMontant()) 
                            .adminId(adminId)
                            .build();

                    NotificationDTO notification = NotificationDTO.builder()
                            .adminId(adminId)
                            .type("ABONNEMENT_EXPIRE")
                            .titre("Renouvellement d'abonnement requis")
                            .abonnement(aboDto)
                            .build();

                    notificationClient.sendNotification(notification);
                    log.info("Notification sent for cabinet: {}", abo.getCabinet().getNom());
                } catch (Exception e) {
                    log.error("Failed to send notification for abonnement {}", abo.getIdAbonnement(), e);
                }
            });
        } else {
            log.info("No expiring abonnements found.");
        }
    }

    // Run every day at midnight to expire outdated abonnements
    @Scheduled(cron = "0 0 0 * * *")
    public void expireAbonnements() {
        log.info("Checking for expired abonnements...");
        List<AbonnementCabinet> activeAbonnements = abonnementRepository.findByStatut(AbonnementStatus.ACTIF);
        LocalDateTime now = LocalDateTime.now();

        for (AbonnementCabinet abo : activeAbonnements) {
            if (abo.getDateFin().isBefore(now)) {
                log.info("Expiring abonnement id: {}", abo.getIdAbonnement());
                abo.setStatut(AbonnementStatus.EXPIRE);

                // Also deactive cabinet
                if (abo.getCabinet() != null) {
                    abo.getCabinet().setActif(false);
                }
                abonnementRepository.save(abo);
            }
        }
    }
}
