package com.example.cabinetservice.scheduler;

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

    // Run every day at 8:00 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void checkExpiringAbonnements() {
        log.info("Checking for expiring abonnements...");
        // Calcule la date dans 7 jours
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        
        // This query needs to be precise in Repository. 
        // For now finding active ones and checking date in memory or using the query defined.
        List<AbonnementCabinet> expiring = abonnementRepository.findExpiringAbonnements(sevenDaysFromNow, AbonnementStatus.ACTIF);
        
        if (!expiring.isEmpty()) {
            log.info("Found {} expiring abonnements.", expiring.size());
            // TODO: Send notification to Notification Service
            // expiring.forEach(abo -> notificationClient.sendExpirationAlert(abo));
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
