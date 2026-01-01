package com.example.rendezvousservice.service.impl;

import com.example.rendezvousservice.client.notification.NotificationClient;
import com.example.rendezvousservice.client.notification.NotificationDTO;
import com.example.rendezvousservice.client.notification.NotificationDossierDTO;
import com.example.rendezvousservice.client.patient.DossierMedicalDTO;
import com.example.rendezvousservice.client.patient.PatientClient;

import com.example.rendezvousservice.client.patient.PatientInfoDTO;
import com.example.rendezvousservice.dto.*;
import com.example.rendezvousservice.entity.RendezVous;
import com.example.rendezvousservice.enums.StatutRendezVous;
import com.example.rendezvousservice.exception.RendezVousException;
import com.example.rendezvousservice.repository.RendezVousRepository;
import com.example.rendezvousservice.service.IRendezVousService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implémentation simplifiée du service de gestion des rendez-vous.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RendezVousServiceImpl implements IRendezVousService {

    private final RendezVousRepository repository;
    private final PatientClient patientClient;
    private final NotificationClient notificationClient;

    // ========== CRUD Rendez-vous ==========

    @Override
    public RendezVousDTO createRendezVous(CreateRendezVousDTO dto) {
        log.info("📝 Création d'un nouveau rendez-vous pour le patient ID: {}", dto.getIdPatient());

        // 1. Vérifier la disponibilité du créneau
        repository.findByMedecinAndDateAndHeure(
                dto.getIdMedecin(),
                dto.getDateRdv(),
                dto.getHeureRdv(),
                StatutRendezVous.ANNULE,
                StatutRendezVous.TERMINE).ifPresent(rdv -> {
                    log.warn("⚠️ Créneau déjà occupé: {} à {}", dto.getDateRdv(), dto.getHeureRdv());
                    throw new RendezVousException("Ce créneau est déjà occupé");
                });

        // 2. Vérifier que le patient existe
        PatientInfoDTO patientInfo = patientClient.getPatientInfo(dto.getIdPatient());
        if (patientInfo == null || patientInfo.getNom().contains("indisponible")) {
            log.error("❌ Patient introuvable: ID {}", dto.getIdPatient());
            throw new RendezVousException("Patient introuvable avec l'ID: " + dto.getIdPatient());
        }

        // 3. Créer le rendez-vous
        RendezVous rendezVous = RendezVous.builder()
                .idPatient(dto.getIdPatient())
                .idMedecin(dto.getIdMedecin())
                .dateRdv(dto.getDateRdv())
                .heureRdv(dto.getHeureRdv())
                .motif(dto.getMotif())
                .statut(StatutRendezVous.CONFIRME)
                .build();

        RendezVous saved = repository.save(rendezVous);
        log.info("✅ Rendez-vous créé avec succès - ID: {}", saved.getId());

        return toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousDTO getRendezVous(Long id) {
        log.info("📖 Récupération du rendez-vous ID: {}", id);

        RendezVous rendezVous = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Rendez-vous introuvable: ID {}", id);
                    return new RendezVousException("Rendez-vous introuvable avec l'ID: " + id);
                });

        return toDTO(rendezVous);
    }

    @Override
    public RendezVousDTO updateRendezVous(Long id, UpdateRendezVousDTO dto) {
        log.info("✏️ Mise à jour du rendez-vous ID: {}", id);

        // 1. Récupérer le rendez-vous existant
        RendezVous rendezVous = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Rendez-vous introuvable: ID {}", id);
                    return new RendezVousException("Rendez-vous introuvable avec l'ID: " + id);
                });

        // 2. Vérifier que le rendez-vous peut être modifié
        if (!rendezVous.peutEtreModifie()) {
            log.warn("⚠️ Tentative de modification d'un RDV terminé ou annulé: ID {}", id);
            throw new RendezVousException("Ce rendez-vous ne peut plus être modifié (statut: " +
                    rendezVous.getStatut() + ")");
        }

        // 3. Vérifier la disponibilité si changement de créneau
        if (dto.getDateRdv() != null || dto.getHeureRdv() != null) {
            LocalDate nouvelleDate = dto.getDateRdv() != null ? dto.getDateRdv() : rendezVous.getDateRdv();
            LocalTime nouvelleHeure = dto.getHeureRdv() != null ? dto.getHeureRdv() : rendezVous.getHeureRdv();

            repository.findByMedecinAndDateAndHeure(
                    rendezVous.getIdMedecin(),
                    nouvelleDate,
                    nouvelleHeure,
                    StatutRendezVous.ANNULE,
                    StatutRendezVous.TERMINE).ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            log.warn("⚠️ Nouveau créneau déjà occupé: {} à {}", nouvelleDate, nouvelleHeure);
                            throw new RendezVousException("Ce créneau est déjà occupé");
                        }
                    });

            rendezVous.setDateRdv(nouvelleDate);
            rendezVous.setHeureRdv(nouvelleHeure);
            log.info("📅 Créneau modifié: {} à {}", nouvelleDate, nouvelleHeure);
        }

        // 4. Mettre à jour les autres champs
        if (dto.getMotif() != null) {
            rendezVous.setMotif(dto.getMotif());
            log.info("📋 Motif modifié: {}", dto.getMotif());
        }

        // 5. Sauvegarder les modifications
        RendezVous updated = repository.save(rendezVous);
        log.info("✅ Rendez-vous mis à jour avec succès");

        return toDTO(updated);
    }

    @Override
    public void deleteRendezVous(Long id) {
        log.info("🗑️ Annulation du rendez-vous ID: {}", id);

        RendezVous rendezVous = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Rendez-vous introuvable: ID {}", id);
                    return new RendezVousException("Rendez-vous introuvable avec l'ID: " + id);
                });

        if (!rendezVous.peutEtreAnnule()) {
            log.warn("⚠️ Tentative d'annulation d'un RDV non annulable: ID {}", id);
            throw new RendezVousException("Ce rendez-vous ne peut pas être annulé (statut: " +
                    rendezVous.getStatut() + ")");
        }

        rendezVous.annuler();
        repository.save(rendezVous);
        log.info("✅ Rendez-vous annulé avec succès");
    }

    @Override
    public RendezVousDTO changeStatut(Long id, ChangeStatutDTO dto) {
        log.info("🔄 Changement de statut du rendez-vous ID: {} vers {}", id, dto.getStatut());

        RendezVous rendezVous = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Rendez-vous introuvable: ID {}", id);
                    return new RendezVousException("Rendez-vous introuvable avec l'ID: " + id);
                });

        StatutRendezVous ancienStatut = rendezVous.getStatut();
        rendezVous.setStatut(dto.getStatut());

        RendezVous updated = repository.save(rendezVous);
        log.info("✅ Statut changé de {} à {}", ancienStatut, dto.getStatut());

        // ⭐ NOTIFICATION UNIQUEMENT SI PASSAGE EN CONSULTATION ⭐
        if (dto.getStatut() == StatutRendezVous.EN_CONSULTATION) {
            notifierMedecinPatientSuivant(updated.getIdMedecin(), updated.getDateRdv());
        }

        return toDTO(updated);
    }

    // ========== Recherches ==========

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousDTO> getRendezVousByPatient(Long patientId) {
        log.info("🔍 Recherche des rendez-vous du patient ID: {}", patientId);

        List<RendezVous> rendezVousList = repository.findByIdPatientAndStatutNot(
                patientId,
                StatutRendezVous.ANNULE);

        log.info("📊 {} rendez-vous trouvés pour le patient ID: {}", rendezVousList.size(), patientId);

        return rendezVousList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousDTO> getRendezVousByMedecinAndDate(Long medecinId, LocalDate date) {
        log.info("🔍 Recherche des rendez-vous du médecin ID: {} pour le {}", medecinId, date);

        List<RendezVous> rendezVousList = repository.findByIdMedecinAndDateRdvAndStatutNot(
                medecinId,
                date,
                StatutRendezVous.ANNULE);

        log.info("📊 {} rendez-vous trouvés pour le médecin ID: {} le {}",
                rendezVousList.size(), medecinId, date);

        return rendezVousList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousDTO> getRendezVousDuJour(Long medecinId) {
        LocalDate aujourdhui = LocalDate.now();
        log.info("📅 Récupération des rendez-vous du jour ({}) pour le médecin ID: {}",
                aujourdhui, medecinId);

        List<RendezVous> rendezVousList = repository.findRendezVousDuJour(
                medecinId,
                aujourdhui,
                StatutRendezVous.ANNULE,
                StatutRendezVous.TERMINE);

        log.info("📊 {} rendez-vous aujourd'hui pour le médecin ID: {}",
                rendezVousList.size(), medecinId);

        return rendezVousList.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ========== Gestion des Disponibilités ==========

    @Override
    @Transactional(readOnly = true)
    public DisponibilitesDTO getDisponibilites(Long medecinId, LocalDate date) {
        log.info("📋 Recherche des disponibilités du médecin ID: {} pour le {}", medecinId, date);

        LocalTime heureDebut = LocalTime.of(8, 0);
        LocalTime heureFin = LocalTime.of(18, 0);
        int intervalleMinutes = 30;

        List<DisponibilitesDTO.CreneauDTO> creneaux = new ArrayList<>();
        LocalTime heureCourante = heureDebut;

        int creneauxDisponibles = 0;

        while (heureCourante.isBefore(heureFin)) {
            boolean disponible = repository.findByMedecinAndDateAndHeure(
                    medecinId,
                    date,
                    heureCourante,
                    StatutRendezVous.ANNULE,
                    StatutRendezVous.TERMINE).isEmpty();

            creneaux.add(DisponibilitesDTO.CreneauDTO.builder()
                    .heure(heureCourante)
                    .disponible(disponible)
                    .build());

            if (disponible) {
                creneauxDisponibles++;
            }

            heureCourante = heureCourante.plusMinutes(intervalleMinutes);
        }

        log.info("📊 {} créneaux disponibles sur {}", creneauxDisponibles, creneaux.size());

        return DisponibilitesDTO.builder()
                .idMedecin(medecinId)
                .date(date)
                .creneaux(creneaux)
                .build();
    }

    // ========== Gestion de la Liste d'Attente ==========

    @Override
    @Transactional(readOnly = true)
    public List<RendezVousDTO> getListeAttente(Long medecinId) {
        LocalDate aujourdhui = LocalDate.now();
        log.info("📝 Récupération de la liste d'attente du médecin ID: {} pour le {}",
                medecinId, aujourdhui);

        List<RendezVous> listeAttente = repository.findListeAttenteByMedecinAndDate(
                medecinId,
                aujourdhui,
                StatutRendezVous.PRESENT);

        log.info("📊 {} patients dans la liste d'attente", listeAttente.size());

        return listeAttente.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RendezVousDTO ajouterEnListeAttente(Long rendezVousId) {
        log.info("➕ Ajout du rendez-vous ID: {} à la liste d'attente", rendezVousId);

        RendezVous rendezVous = repository.findById(rendezVousId)
                .orElseThrow(() -> {
                    log.error("❌ Rendez-vous introuvable: ID {}", rendezVousId);
                    return new RendezVousException("Rendez-vous introuvable avec l'ID: " + rendezVousId);
                });

        if (!rendezVous.peutEtreAjouteEnAttente()) {
            log.warn("⚠️ Rendez-vous non éligible pour la liste d'attente: statut {}",
                    rendezVous.getStatut());
            throw new RendezVousException("Ce rendez-vous ne peut pas être ajouté à la liste d'attente " +
                    "(statut actuel: " + rendezVous.getStatut() + ")");
        }

        Integer maxOrdre = repository.findMaxOrdrePassage(
                rendezVous.getIdMedecin(),
                rendezVous.getDateRdv(),
                StatutRendezVous.PRESENT);
        int nouvelOrdre = (maxOrdre != null ? maxOrdre : 0) + 1;

        try {
            rendezVous.ajouterEnListeAttente(nouvelOrdre);
            RendezVous updated = repository.save(rendezVous);

            log.info("✅ Patient ajouté à la liste d'attente avec l'ordre: {}", nouvelOrdre);
            log.info("🕐 Heure d'arrivée enregistrée: {}", updated.getHeureArrivee());

            return toDTO(updated);
        } catch (IllegalStateException e) {
            log.error("❌ Erreur lors de l'ajout à la liste d'attente: {}", e.getMessage());
            throw new RendezVousException(e.getMessage());
        }
    }

    @Override
    public RendezVousDTO retirerDeListeAttente(Long rendezVousId) {
        log.info("➖ Retrait du rendez-vous ID: {} de la liste d'attente", rendezVousId);

        RendezVous rendezVous = repository.findById(rendezVousId)
                .orElseThrow(() -> {
                    log.error("❌ Rendez-vous introuvable: ID {}", rendezVousId);
                    return new RendezVousException("Rendez-vous introuvable avec l'ID: " + rendezVousId);
                });

        if (rendezVous.getOrdrePassage() == null) {
            log.warn("⚠️ Le rendez-vous n'est pas dans la liste d'attente: ID {}", rendezVousId);
            throw new RendezVousException("Ce rendez-vous n'est pas dans la liste d'attente");
        }

        rendezVous.retirerDeListeAttente();
        RendezVous updated = repository.save(rendezVous);

        log.info("✅ Patient retiré de la liste d'attente");

        return toDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public RendezVousDTO getPatientSuivant(Long medecinId) {
        LocalDate aujourdhui = LocalDate.now();
        log.info("👤 Recherche du patient suivant pour le médecin ID: {} le {}",
                medecinId, aujourdhui);

        RendezVous patientSuivant = repository.findPatientSuivant(
                medecinId,
                aujourdhui,
                StatutRendezVous.PRESENT).orElseThrow(() -> {
                    log.warn("⚠️ Aucun patient en attente pour le médecin ID: {}", medecinId);
                    return new RendezVousException("Aucun patient en attente pour ce médecin");
                });

        log.info("✅ Patient suivant trouvé - Ordre: {}, RDV ID: {}",
                patientSuivant.getOrdrePassage(), patientSuivant.getId());

        return toDTO(patientSuivant);
    }

    // ========== Méthodes Privées ==========

    /**
     * ⭐ Notifie le médecin du patient suivant avec son DOSSIER MÉDICAL COMPLET.
     * Appelée uniquement lors du passage en consultation.
     */
    private void notifierMedecinPatientSuivant(Long medecinId, LocalDate dateRdv) {
        try {
            // Récupérer le patient suivant dans la liste d'attente
            Optional<RendezVous> patientSuivantOpt = repository.findPatientSuivant(
                    medecinId,
                    dateRdv,
                    StatutRendezVous.PRESENT);

            if (patientSuivantOpt.isPresent()) {
                RendezVous patientSuivant = patientSuivantOpt.get();

                // Récupérer les infos basiques du patient suivant
                PatientInfoDTO patientInfo = patientClient.getPatientInfo(patientSuivant.getIdPatient());

                // Récupérer le DOSSIER MÉDICAL COMPLET du patient suivant
                DossierMedicalDTO dossier = patientClient.getDossierMedical(patientSuivant.getIdPatient());

                NotificationDossierDTO dossierComplet = NotificationDossierDTO.builder()
                        .nom(patientInfo.getNom())
                        .prenom(patientInfo.getPrenom())
                        .email(patientInfo.getEmail())
                        .telephone(patientInfo.getTelephone())
                        .idDossier(dossier.getIdDossier())
                        .antecedentsMedicaux(dossier.getAntecedentsMedicaux())
                        .antecedentsChirurgicaux(dossier.getAntecedentsChirurgicaux())
                        .allergies(dossier.getAllergies())
                        .groupeSanguin(dossier.getGroupeSanguin())
                        .remarques(dossier.getRemarques())
                        .dateCreation(dossier.getDateCreation())
                        .build();

                // Créer la notification avec le dossier complet
                NotificationDTO notification = NotificationDTO.builder()
                        .userId(medecinId)
                        .type("PATIENT_SUIVANT")
                        .titre(String.format("Patient Suivant - N°%d : %s %s",
                                patientSuivant.getOrdrePassage(),
                                patientInfo.getPrenom(),
                                patientInfo.getNom()))
                        .dossierPatient(dossierComplet)
                        .build();

                notificationClient.sendNotification(notification);

                log.info("📧 Notification envoyée au médecin ID: {} - Patient suivant: {} {} (Ordre: {})",
                        medecinId,
                        patientInfo.getPrenom(),
                        patientInfo.getNom(),
                        patientSuivant.getOrdrePassage());
                log.info("📋 Dossier médical inclus - Allergies: {}, Groupe sanguin: {}",
                        dossier.getAllergies() != null ? dossier.getAllergies() : "Aucune",
                        dossier.getGroupeSanguin() != null ? dossier.getGroupeSanguin() : "Non renseigné");
            } else {
                log.info("ℹ️ Aucun patient suivant dans la liste d'attente");
            }
        } catch (Exception e) {
            log.warn("⚠️ Impossible d'envoyer la notification du patient suivant: {}", e.getMessage());
            log.debug("Détails de l'erreur:", e);
        }
    }

    /**
     * Convertit une entité RendezVous en DTO (simplifié sans infos externes).
     */
    private RendezVousDTO toDTO(RendezVous rendezVous) {
        return RendezVousDTO.builder()
                .id(rendezVous.getId())
                .idPatient(rendezVous.getIdPatient())
                .idMedecin(rendezVous.getIdMedecin())
                .dateRdv(rendezVous.getDateRdv())
                .heureRdv(rendezVous.getHeureRdv())
                .motif(rendezVous.getMotif())
                .statut(rendezVous.getStatut())
                .ordrePassage(rendezVous.getOrdrePassage())
                .heureArrivee(rendezVous.getHeureArrivee())
                .dateCreation(rendezVous.getDateCreation())
                .dateModification(rendezVous.getDateModification())
                .build();
    }
}