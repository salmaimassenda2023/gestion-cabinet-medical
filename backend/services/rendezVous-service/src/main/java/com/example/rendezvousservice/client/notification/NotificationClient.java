package com.example.rendezvousservice.client.notification;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Client Feign pour communiquer avec le microservice Notification.
 */
@FeignClient(
        name = "notification-service",
        url = "http://localhost:8086",  
        fallback = NotificationClientFallback.class
)
public interface NotificationClient {

    /**
     * Envoie une notification à un utilisateur (médecin).
     *
     * @param notification les données de la notification
     */
    @PostMapping("/api/notification")
    void sendNotification(@RequestBody NotificationDTO notification);
}
