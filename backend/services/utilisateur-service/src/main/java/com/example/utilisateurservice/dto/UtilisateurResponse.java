package com.example.utilisateurservice.dto;


import com.example.utilisateurservice.entity.Utilisateur;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class UtilisateurResponse {
    private Long idUtilisateur;
    private String keycloakId;
    private String login;
    private String nom;
    private String prenom;
    private String numTel;
    private String signature;
    private Utilisateur.Role role;
    private Long idCabinet;
    private Boolean actif;
    private LocalDateTime dateCreation;
}