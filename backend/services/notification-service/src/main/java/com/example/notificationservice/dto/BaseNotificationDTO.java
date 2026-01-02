package com.example.notificationservice.dto;


import lombok.*;

// DTO de base pour toutes les notifications
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseNotificationDTO {
    private Long id;
    private String type;
    private String titre;
    private String message;
    private Boolean lu;
    private String dateEnvoi;
}