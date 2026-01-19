package com.example.notificationservice.service.impl;

import com.example.notificationservice.dto.BaseNotificationDTO;
import com.example.notificationservice.dto.NotificationRequestDTO;
import com.example.notificationservice.dto.patient.PatientSuivantResponseDTO;
import com.example.notificationservice.dto.abonnement_cabinet.*;
import com.example.notificationservice.dto.patient.DossierPatientDTO;
import com.example.notificationservice.entity.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import com.example.notificationservice.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository repository;
    private final ObjectMapper objectMapper;

    // ENVOI - PATIENT SUIVANT (Médecin)

    @Override
    public void sendNotification(NotificationRequestDTO request) {
        try {
            log.info("Traitement notification - Type: {}", request.getType());

            if ("PATIENT_SUIVANT".equals(request.getType())) {
                processPatientSuivant(request);
            } else if ("ABONNEMENT_EXPIRE".equals(request.getType())) {
                processAbonnementExpire(request);
            } else {
                log.warn("Type de notification inconnu: {}", request.getType());
            }

        } catch (Exception e) {
            log.error("Erreur traitement notification", e);
            throw new RuntimeException("Erreur lors du traitement de la notification", e);
        }
    }

    private void processPatientSuivant(NotificationRequestDTO request) throws JsonProcessingException {
        Long medecinId = request.getTargetId();
        log.info("Envoi notification PATIENT_SUIVANT au médecin ID: {}", medecinId);

        String dossierJson = request.getDossierPatient() != null
                ? objectMapper.writeValueAsString(request.getDossierPatient())
                : null;
        Long cabinetId = request.getCabinetId() != null ? request.getCabinetId() : 0L;

        Notification notification = Notification.builder()
                .idDestinataire(medecinId)
                .type("PATIENT_SUIVANT")
                .titre(request.getTitre())
                .dossierPatientJson(dossierJson)
                .idCabinet(cabinetId)
                .lu(false)
                .build();

        repository.save(notification);
    }

    private void processAbonnementExpire(NotificationRequestDTO request) throws JsonProcessingException {
        Long adminId = request.getTargetId();
        log.info("Envoi notification ABONNEMENT_EXPIRE à l'admin ID: {}", adminId);

        if (request.getAbonnement() != null && request.getAbonnement().getAdminId() == null) {
            request.getAbonnement().setAdminId(adminId);
        }

        String abonnementJson = request.getAbonnement() != null
                ? objectMapper.writeValueAsString(request.getAbonnement())
                : null;

        String nomCabinet = request.getAbonnement() != null ? request.getAbonnement().getNomCabinet() : "Inconnu";
        Integer joursRestants = request.getAbonnement() != null ? request.getAbonnement().getJoursRestants() : 0;
        String dateExpiration = request.getAbonnement() != null ? request.getAbonnement().getDateExpiration() : "";
        Double montant = request.getAbonnement() != null ? request.getAbonnement().getMontant() : 0.0;

        String titre = String.format("Abonnement expire dans %d jours - %s", joursRestants, nomCabinet);
        String message = String.format(
                "Votre abonnement pour le cabinet '%s' expire le %s. Veuillez renouveler votre abonnement. Montant : %.2f DH",
                nomCabinet, dateExpiration, montant);

        Notification notification = Notification.builder()
                .idDestinataire(adminId)
                .idCabinet(request.getAbonnement() != null ? request.getAbonnement().getCabinetId() : 0L)
                .type("ABONNEMENT_EXPIRE")
                .titre(titre)
                .message(message)
                .abonnementJson(abonnementJson)
                .lu(false)
                .build();

        repository.save(notification);
    }

    // RÉCUPÉRATION - PATIENT SUIVANT (Médecin)

    @Override
    @Transactional(readOnly = true)
    public List<PatientSuivantResponseDTO> getPatientSuivantNotifications(Long medecinId) {
        log.info("Récupération notifications PATIENT_SUIVANT - Médecin ID: {}", medecinId);

        List<Notification> notifications = repository
                .findByIdDestinataireAndType(medecinId, "PATIENT_SUIVANT");

        return notifications.stream()
                .map(this::toPatientSuivantDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSuivantResponseDTO> getUnreadPatientSuivantNotifications(Long medecinId) {
        log.info("Récupération notifications PATIENT_SUIVANT non lues - Médecin ID: {}", medecinId);

        List<Notification> notifications = repository
                .findByIdDestinataireAndTypeAndLuFalse(medecinId, "PATIENT_SUIVANT");

        return notifications.stream()
                .map(this::toPatientSuivantDTO)
                .collect(Collectors.toList());
    }

    // RÉCUPÉRATION - ABONNEMENT (Admin)

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementExpirationResponseDTO> getAbonnementNotifications(Long adminId) {
        log.info("Récupération notifications ABONNEMENT_EXPIRE - Admin ID: {}", adminId);

        List<Notification> notifications = repository
                .findByIdDestinataireAndType(adminId, "ABONNEMENT_EXPIRE");

        return notifications.stream()
                .map(this::toAbonnementDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbonnementExpirationResponseDTO> getUnreadAbonnementNotifications(Long adminId) {
        log.info("Récupération notifications ABONNEMENT_EXPIRE non lues - Admin ID: {}", adminId);

        List<Notification> notifications = repository
                .findByIdDestinataireAndTypeAndLuFalse(adminId, "ABONNEMENT_EXPIRE");

        return notifications.stream()
                .map(this::toAbonnementDTO)
                .collect(Collectors.toList());
    }

    // GESTION COMMUNE

    @Override
    @Transactional(readOnly = true)
    public BaseNotificationDTO getNotificationById(Long notificationId) {
        log.info("Récupération notification - ID: {}", notificationId);

        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable: " + notificationId));

        if ("PATIENT_SUIVANT".equals(notification.getType())) {
            return toPatientSuivantDTO(notification);
        } else if ("ABONNEMENT_EXPIRE".equals(notification.getType())) {
            return toAbonnementDTO(notification);
        }

        return null;
    }

    @Override
    public void markAsRead(Long notificationId) {
        log.info("Marquage notification comme lue - ID: {}", notificationId);

        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification introuvable: " + notificationId));

        if (!notification.getLu()) {
            notification.marquerCommeLue();
            repository.save(notification);
            log.info("Notification marquée comme lue");
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        log.info("Marquage toutes notifications comme lues - User ID: {}", userId);

        List<Notification> notifications = repository
                .findByIdDestinataireAndLuFalseOrderByDateEnvoiDesc(userId);

        if (!notifications.isEmpty()) {
            notifications.forEach(Notification::marquerCommeLue);
            repository.saveAll(notifications);
            log.info("{} notifications marquées", notifications.size());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnread(Long userId) {
        return repository.countByIdDestinataireAndLuFalse(userId);
    }

    @Override
    @Transactional
    public void cleanOldNotifications() {
        log.info("Nettoyage notifications > 90 jours");
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        repository.deleteByDateEnvoiBefore(cutoffDate);
    }

    // CONVERSIONS PRIVÉES (2 méthodes séparées)

    /**
     * Convertir en PatientSuivantResponseDTO (pour Médecin)
     */
    private PatientSuivantResponseDTO toPatientSuivantDTO(Notification notification) {
        try {
            // Désérialiser UNIQUEMENT le dossier patient
            DossierPatientDTO dossierPatient = null;
            if (notification.getDossierPatientJson() != null) {
                dossierPatient = objectMapper.readValue(
                        notification.getDossierPatientJson(),
                        DossierPatientDTO.class);
            }

            return PatientSuivantResponseDTO.builder()
                    .id(notification.getIdNotification())
                    .type(notification.getType())
                    .titre(notification.getTitre())
                    .message(notification.getMessage())
                    .lu(notification.getLu())
                    .dateEnvoi(notification.getDateEnvoi().toString())
                    .dossierPatient(dossierPatient)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Erreur désérialisation dossier patient - Notification ID: {}",
                    notification.getIdNotification(), e);

            // Retourner DTO minimal en cas d'erreur
            return PatientSuivantResponseDTO.builder()
                    .id(notification.getIdNotification())
                    .type(notification.getType())
                    .titre(notification.getTitre())
                    .lu(notification.getLu())
                    .dateEnvoi(notification.getDateEnvoi().toString())
                    .build();
        }
    }

    /**
     * Convertir en AbonnementExpirationResponseDTO (pour Admin)
     */
    private AbonnementExpirationResponseDTO toAbonnementDTO(Notification notification) {
        try {
            // Désérialiser UNIQUEMENT les infos d'abonnement
            AbonnementInfoDTO abonnementInfo = null;
            if (notification.getAbonnementJson() != null) {
                AbonnementExpirationNotificationDTO expirationDTO = objectMapper.readValue(
                        notification.getAbonnementJson(),
                        AbonnementExpirationNotificationDTO.class);

                abonnementInfo = AbonnementInfoDTO.builder()
                        .cabinetId(expirationDTO.getCabinetId())
                        .nomCabinet(expirationDTO.getNomCabinet())
                        .dateExpiration(expirationDTO.getDateExpiration())
                        .joursRestants(expirationDTO.getJoursRestants())
                        .montant(expirationDTO.getMontant())
                        .build();
            }

            return AbonnementExpirationResponseDTO.builder()
                    .id(notification.getIdNotification())
                    .type(notification.getType())
                    .titre(notification.getTitre())
                    .message(notification.getMessage())
                    .lu(notification.getLu())
                    .dateEnvoi(notification.getDateEnvoi().toString())
                    .abonnementInfo(abonnementInfo) 
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Erreur désérialisation infos abonnement - Notification ID: {}",
                    notification.getIdNotification(), e);

            // Retourner DTO minimal en cas d'erreur
            return AbonnementExpirationResponseDTO.builder()
                    .id(notification.getIdNotification())
                    .type(notification.getType())
                    .titre(notification.getTitre())
                    .message(notification.getMessage())
                    .lu(notification.getLu())
                    .dateEnvoi(notification.getDateEnvoi().toString())
                    .build();
        }
    }
}