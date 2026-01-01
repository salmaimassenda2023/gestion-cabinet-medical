package com.example.notificationservice.repository;

import com.example.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupérer par type
    List<Notification> findByIdDestinataireAndType(Long userId, String type);

    // Récupérer non lues par type
    List<Notification> findByIdDestinataireAndTypeAndLuFalse(Long userId, String type);

    // Récupérer toutes les non lues
    List<Notification> findByIdDestinataireAndLuFalseOrderByDateEnvoiDesc(Long userId);

    // Compter les non lues
    Long countByIdDestinataireAndLuFalse(Long userId);

    // Supprimer les anciennes
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.dateEnvoi < :date")
    void deleteByDateEnvoiBefore(LocalDateTime date);
}
