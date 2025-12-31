package com.example.rendezvousservice.repository;

import com.example.rendezvousservice.entity.RendezVous;
import com.example.rendezvousservice.enums.StatutRendezVous;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RendezVousRepository extends JpaRepository<RendezVous, Long> {

    // ========== Recherche par Patient ==========

    /**
     * Trouve tous les rendez-vous d'un patient.
     */
    List<RendezVous> findByIdPatient(Long idPatient);

    /**
     * Trouve les rendez-vous d'un patient (sauf annulés).
     */
    List<RendezVous> findByIdPatientAndStatutNot(Long idPatient, StatutRendezVous statut);

    // ========== Recherche par Médecin ==========

    /**
     * Trouve tous les rendez-vous d'un médecin pour une date donnée.
     */
    List<RendezVous> findByIdMedecinAndDateRdv(Long idMedecin, LocalDate dateRdv);

    /**
     * Trouve les rendez-vous d'un médecin (sauf annulés).
     */
    List<RendezVous> findByIdMedecinAndDateRdvAndStatutNot(
            Long idMedecin,
            LocalDate dateRdv,
            StatutRendezVous statut
    );

    // ========== Vérification de Disponibilité ==========

    /**
     * Vérifie si un créneau est disponible pour un médecin.
     * Retourne le RDV existant si le créneau est occupé, sinon Optional.empty().
     *
     * CORRECTION: Utilisation de l'enum StatutRendezVous au lieu de String
     */
    @Query("SELECT r FROM RendezVous r WHERE r.idMedecin = :idMedecin " +
            "AND r.dateRdv = :dateRdv AND r.heureRdv = :heureRdv " +
            "AND r.statut NOT IN (:statutAnnule, :statutTermine)")
    Optional<RendezVous> findByMedecinAndDateAndHeure(
            @Param("idMedecin") Long idMedecin,
            @Param("dateRdv") LocalDate dateRdv,
            @Param("heureRdv") LocalTime heureRdv,
            @Param("statutAnnule") StatutRendezVous statutAnnule,
            @Param("statutTermine") StatutRendezVous statutTermine
    );

    // ========== Gestion de la Liste d'Attente ==========

    /**
     * Récupère la liste d'attente complète d'un médecin pour une date.
     * Triée par ordre de passage croissant.
     *
     * CORRECTION: Utilisation de l'enum StatutRendezVous au lieu de String
     */
    @Query("SELECT r FROM RendezVous r WHERE r.idMedecin = :idMedecin " +
            "AND r.dateRdv = :dateRdv AND r.statut = :statutPresent " +
            "ORDER BY r.ordrePassage ASC")
    List<RendezVous> findListeAttenteByMedecinAndDate(
            @Param("idMedecin") Long idMedecin,
            @Param("dateRdv") LocalDate dateRdv,
            @Param("statutPresent") StatutRendezVous statutPresent
    );

    /**
     * Récupère le patient suivant dans la file d'attente.
     * Retourne le RDV avec le plus petit ordre de passage.
     *
     * CORRECTION: Utilisation de l'enum StatutRendezVous au lieu de String
     */
    @Query("SELECT r FROM RendezVous r WHERE r.idMedecin = :idMedecin " +
            "AND r.dateRdv = :dateRdv AND r.statut = :statutPresent " +
            "ORDER BY r.ordrePassage ASC")
    Optional<RendezVous> findPatientSuivant(
            @Param("idMedecin") Long idMedecin,
            @Param("dateRdv") LocalDate dateRdv,
            @Param("statutPresent") StatutRendezVous statutPresent
    );

    /**
     * Trouve le numéro d'ordre maximum actuel.
     * Utilisé pour attribuer un ordre au prochain patient.
     *
     * CORRECTION: Utilisation de l'enum StatutRendezVous au lieu de String
     */
    @Query("SELECT COALESCE(MAX(r.ordrePassage), 0) FROM RendezVous r " +
            "WHERE r.idMedecin = :idMedecin AND r.dateRdv = :dateRdv " +
            "AND r.statut = :statutPresent")
    Integer findMaxOrdrePassage(
            @Param("idMedecin") Long idMedecin,
            @Param("dateRdv") LocalDate dateRdv,
            @Param("statutPresent") StatutRendezVous statutPresent
    );

    /**
     * Récupère l'agenda complet d'un médecin pour une date.
     * Exclut les RDV annulés et terminés.
     * Triés par heure croissante.
     *
     * CORRECTION: Utilisation de l'enum StatutRendezVous au lieu de String
     */
    @Query("SELECT r FROM RendezVous r WHERE r.idMedecin = :idMedecin " +
            "AND r.dateRdv = :dateRdv " +
            "AND r.statut NOT IN (:statutAnnule, :statutTermine) " +
            "ORDER BY r.heureRdv ASC")
    List<RendezVous> findRendezVousDuJour(
            @Param("idMedecin") Long idMedecin,
            @Param("dateRdv") LocalDate dateRdv,
            @Param("statutAnnule") StatutRendezVous statutAnnule,
            @Param("statutTermine") StatutRendezVous statutTermine
    );
}