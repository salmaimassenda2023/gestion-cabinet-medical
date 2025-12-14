package com.example.utilisateurservice.dto;


import com.example.utilisateurservice.entity.Utilisateur;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
public class UtilisateurRequest {

    @NotBlank(message = "Le login est obligatoire")
    @Size(min = 3, max = 50)
    private String login;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
    private String numTel;

    private String signature;

    @NotNull(message = "Le rôle est obligatoire")
    private Utilisateur.Role role;

    private Long idCabinet;
}
