package com.example.utilisateurservice.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUtilisateur;

    @Column(unique = true, nullable = false)
    private String keycloakId; // ID de l'utilisateur dans Keycloak

    @Column(unique = true, nullable = false, length = 50)
    private String login;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 15)
    private String numTel;

    @Lob
    private String signature; // Base64 pour les médecins

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "id_cabinet")
    private Long idCabinet;

    @Column(nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    public enum Role {
        SUPER_ADMIN,
        ADMIN,
        MEDECIN,
        SECRETAIRE
    }
}