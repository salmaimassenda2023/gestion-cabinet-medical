package com.example.rendezvousservice.client.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback pour le client Notification en cas d'indisponibilité du service.
 */
@Component
@Slf4j
public class NotificationClientFallback implements NotificationClient {

    @Override
    public void sendNotification(NotificationDTO notification) {
        log.warn("️ Fallback: Service Notification indisponible");
        log.warn("Notification non envoyée au médecin ID: {} - Type: {}",
                notification.getUserId(), notification.getType());
    }
}
