package com.example.rendezvousservice.client.patient;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO léger pour les informations basiques du patient.
 * Utilisé par les autres microservices pour éviter de transférer le dossier médical.
 *
 *  Avantages :
 * - Plus rapide (moins de données)
 * - Optimisé pour les listes
 * - Pas de surcharge réseau
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfoDTO {
    private Long id;
    private String cin;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String sexe;
    private String telephone;
    private String email;
    private String adresse;
    private String typeMutuelle;
    private String numeroMutuelle;
    private Long idCabinet;

}
