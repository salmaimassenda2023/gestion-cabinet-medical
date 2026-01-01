package com.example.notificationservice.service;

import com.example.notificationservice.dto.*;

import com.example.notificationservice.dto.abonnement_cabinet.AbonnementExpirationResponseDTO;
import com.example.notificationservice.dto.patient.PatientSuivantResponseDTO;

import java.util.List;

public interface INotificationService {

    // ========== ENVOI DES NOTIFICATIONS ==========
    void sendNotification(NotificationRequestDTO request);

    // ========== RÉCUPÉRATION POUR MÉDECIN ==========
    List<PatientSuivantResponseDTO> getPatientSuivantNotifications(Long medecinId);

    List<PatientSuivantResponseDTO> getUnreadPatientSuivantNotifications(Long medecinId);

    // ========== RÉCUPÉRATION POUR ADMIN ==========
    List<AbonnementExpirationResponseDTO> getAbonnementNotifications(Long adminId);

    List<AbonnementExpirationResponseDTO> getUnreadAbonnementNotifications(Long adminId);

    // ========== GESTION COMMUNE ==========
    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    Long countUnread(Long userId);

    void cleanOldNotifications();
}