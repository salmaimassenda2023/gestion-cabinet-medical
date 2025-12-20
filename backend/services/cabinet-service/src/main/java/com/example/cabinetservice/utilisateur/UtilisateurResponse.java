package com.example.cabinetservice.utilisateur;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponse {
    private Long idUtilisateur;
    private String keycloakId;
    private String login;
    private String nom;
    private String prenom;
    private String numTel;
    private String signature;
    private String role;
    private Long idCabinet;
    private Boolean actif;
    private LocalDateTime dateCreation;
}
