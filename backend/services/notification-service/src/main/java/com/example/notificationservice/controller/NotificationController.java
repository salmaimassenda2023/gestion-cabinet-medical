package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationRequestDTO;

import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequestDTO request) {
        log.info("Reçu une demande de notification - Type: {}", request.getType());
        notificationService.sendNotification(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/patient-suivant/{medecinId}")
    public ResponseEntity<java.util.List<com.example.notificationservice.dto.patient.PatientSuivantResponseDTO>> getPatientSuivantNotifications(
            @PathVariable Long medecinId) {
        return ResponseEntity.ok(notificationService.getPatientSuivantNotifications(medecinId));
    }

    @GetMapping("/abonnement/{adminId}")
    public ResponseEntity<java.util.List<com.example.notificationservice.dto.abonnement_cabinet.AbonnementExpirationResponseDTO>> getAbonnementNotifications(
            @PathVariable Long adminId) {
        return ResponseEntity.ok(notificationService.getAbonnementNotifications(adminId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<com.example.notificationservice.dto.BaseNotificationDTO> getNotificationById(
            @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> countUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countUnread(userId));
    }
}
