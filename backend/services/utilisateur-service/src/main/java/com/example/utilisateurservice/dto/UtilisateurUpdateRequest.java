package com.example.utilisateurservice.dto;


import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurUpdateRequest {

    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Numéro de téléphone invalide")
    private String numTel;

    private String signature;

    // Note : On ne peut pas changer le rôle ni le cabinet via cette méthode
}