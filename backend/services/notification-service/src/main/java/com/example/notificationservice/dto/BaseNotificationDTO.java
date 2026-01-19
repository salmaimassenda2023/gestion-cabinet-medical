package com.example.notificationservice.dto;


import lombok.*;
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