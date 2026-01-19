package com.example.rendezvousservice.service;

import com.example.rendezvousservice.dto.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Interface du service de gestion des rendez-vous médicaux.
 * Définit toutes les opérations métier disponibles.
 */
public interface IRendezVousService {

    // ========== CRUD Rendez-vous ==========

    /**
     * Crée un nouveau rendez-vous médical.
     * Vérifie la disponibilité du créneau et l'existence du patient/médecin.
     *
     *
     * @param dto les données du rendez-vous à créer
     * @return le rendez-vous créé avec toutes les informations
     * @throws com.example.rendezvousservice.exception.RendezVousException si le
     *                                                                     créneau
     *                                                                     est
     *                                                                     occupé ou
     *                                                                     si
     *                                                                     patient/médecin
     *                                                                     introuvable
     */
    RendezVousDTO createRendezVous(CreateRendezVousDTO dto);

    /**
     * Récupère les détails complets d'un rendez-vous.
     *
     * @param id l'identifiant du rendez-vous
     * @return le rendez-vous avec les informations patient et médecin
     * @throws com.example.rendezvousservice.exception.RendezVousException si le
     *                                                                     rendez-vous
     *                                                                     n'existe
     *                                                                     pas
     */
    RendezVousDTO getRendezVous(Long id);

    /**
     * Met à jour les informations d'un rendez-vous existant.
     * Vérifie que le rendez-vous peut être modifié (pas terminé ou annulé).
     * Si changement de créneau, vérifie la disponibilité.
     *
     * @param id  l'identifiant du rendez-vous
     * @param dto les nouvelles données du rendez-vous
     * @return le rendez-vous mis à jour
     * @throws com.example.rendezvousservice.exception.RendezVousException si
     *                                                                     modification
     *                                                                     impossible
     */
    RendezVousDTO updateRendezVous(Long id, UpdateRendezVousDTO dto);

    /**
     * Annule un rendez-vous.
     * Le rendez-vous ne doit pas être déjà terminé ou annulé.
     * Envoie une notification d'annulation au patient.
     *
     * @param id l'identifiant du rendez-vous à annuler
     * @throws com.example.rendezvousservice.exception.RendezVousException si
     *                                                                     annulation
     *                                                                     impossible
     */
    void deleteRendezVous(Long id);

    /**
     * Change le statut d'un rendez-vous.
     *
     * @param id  l'identifiant du rendez-vous
     * @param dto le nouveau statut et éventuellement des remarques
     * @return le rendez-vous avec le statut mis à jour
     * @throws com.example.rendezvousservice.exception.RendezVousException si le
     *                                                                     rendez-vous
     *                                                                     n'existe
     *                                                                     pas
     */
    RendezVousDTO changeStatut(Long id, ChangeStatutDTO dto);

    // ========== Recherches ==========

    /**
     * Récupère tous les rendez-vous d'un patient (sauf annulés).
     *
     * @param patientId l'identifiant du patient
     * @return la liste des rendez-vous du patient
     */
    List<RendezVousDTO> getRendezVousByPatient(Long patientId);

    /**
     * Récupère tous les rendez-vous d'un médecin pour une date donnée (sauf
     * annulés).
     *
     * @param medecinId l'identifiant du médecin
     * @param date      la date recherchée
     * @return la liste des rendez-vous du médecin pour cette date
     */
    List<RendezVousDTO> getRendezVousByMedecinAndDate(Long medecinId, LocalDate date);

    /**
     * Récupère tous les rendez-vous du jour pour un médecin.
     *
     * @param medecinId l'identifiant du médecin
     * @return la liste des rendez-vous du jour
     */
    List<RendezVousDTO> getRendezVousDuJour(Long medecinId);

    // ========== Gestion des Disponibilités ==========

    /**
     * Retourne les créneaux horaires disponibles pour un médecin à une date donnée.
     * Génère des créneaux de 30 minutes entre 8h et 18h.
     *
     * @param medecinId l'identifiant du médecin
     * @param date      la date recherchée
     * @return la liste des créneaux avec leur disponibilité
     */
    DisponibilitesDTO getDisponibilites(Long medecinId, LocalDate date);

    // ========== Gestion de la Liste d'Attente ==========

    /**
     * Récupère la liste d'attente complète d'un médecin pour aujourd'hui.
     * Triée par ordre de passage.
     *
     * @param medecinId l'identifiant du médecin
     * @return la liste des patients en attente
     */
    List<RendezVousDTO> getListeAttente(Long medecinId);

    /**
     * Ajoute un patient à la liste d'attente.
     * Enregistre l'heure d'arrivée et attribue un ordre de passage.
     * Change le statut du rendez-vous à PRESENT.
     *
     * @param rendezVousId l'identifiant du rendez-vous
     * @return le rendez-vous mis à jour
     * @throws com.example.rendezvousservice.exception.RendezVousException si le
     *                                                                     rendez-vous
     *                                                                     ne peut
     *                                                                     pas être
     *                                                                     ajouté
     */
    RendezVousDTO ajouterEnListeAttente(Long rendezVousId);

    /**
     * Retire un patient de la liste d'attente.
     * Supprime l'ordre de passage et l'heure d'arrivée.
     * Remet le statut à CONFIRME.
     *
     * @param rendezVousId l'identifiant du rendez-vous
     * @return le rendez-vous mis à jour
     * @throws com.example.rendezvousservice.exception.RendezVousException si le
     *                                                                     rendez-vous
     *                                                                     n'existe
     *                                                                     pas
     */
    RendezVousDTO retirerDeListeAttente(Long rendezVousId);

    /**
     * Récupère le patient suivant dans la liste d'attente.
     * Retourne le patient avec le plus petit ordre de passage.
     *
     * @param medecinId l'identifiant du médecin
     * @return le prochain patient à consulter ou null si aucun patient en attente
     */
    RendezVousDTO getPatientSuivant(Long medecinId);
}
