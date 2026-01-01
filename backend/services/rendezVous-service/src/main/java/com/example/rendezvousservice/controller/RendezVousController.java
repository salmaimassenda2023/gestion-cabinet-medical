package com.example.rendezvousservice.controller;

import com.example.rendezvousservice.dto.*;
import com.example.rendezvousservice.service.IRendezVousService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller REST pour la gestion des rendez-vous médicaux.
 */
@RestController
@RequestMapping("/api/rendezvous")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class RendezVousController {

    private final IRendezVousService service;

    // ========== CRUD Rendez-vous ==========

    /**
     * Créer un nouveau rendez-vous.
     * POST /api/rendezvous
     */
    @PostMapping
    public ResponseEntity<RendezVousDTO> createRendezVous(
            @Valid @RequestBody CreateRendezVousDTO dto) {
        log.info("📝 POST /api/rendezvous - Création d'un rendez-vous");
        RendezVousDTO created = service.createRendezVous(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Récupérer un rendez-vous par son ID.
     * GET /api/rendezvous/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RendezVousDTO> getRendezVous(@PathVariable Long id) {
        log.info("📖 GET /api/rendezvous/{} - Détails du rendez-vous", id);
        RendezVousDTO rendezVous = service.getRendezVous(id);
        return ResponseEntity.ok(rendezVous);
    }

    /**
     * Modifier un rendez-vous existant.
     * PUT /api/rendezvous/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<RendezVousDTO> updateRendezVous(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRendezVousDTO dto) {
        log.info("✏️ PUT /api/rendezvous/{} - Modification du rendez-vous", id);
        RendezVousDTO updated = service.updateRendezVous(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Annuler un rendez-vous.
     * DELETE /api/rendezvous/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRendezVous(@PathVariable Long id) {
        log.info("🗑️ DELETE /api/rendezvous/{} - Annulation du rendez-vous", id);
        service.deleteRendezVous(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Changer le statut d'un rendez-vous.
     * PUT /api/rendezvous/{id}/statut
     */
    @PutMapping("/{id}/statut")
    public ResponseEntity<RendezVousDTO> changeStatut(
            @PathVariable Long id,
            @Valid @RequestBody ChangeStatutDTO dto) {
        log.info("🔄 PUT /api/rendezvous/{}/statut - Changement de statut", id);
        RendezVousDTO updated = service.changeStatut(id, dto);
        return ResponseEntity.ok(updated);
    }

    // ========== Recherches ==========

    /**
     * Récupérer tous les rendez-vous d'un patient.
     * GET /api/rendezvous/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<RendezVousDTO>> getRendezVousByPatient(
            @PathVariable Long patientId) {
        log.info("🔍 GET /api/rendezvous/patient/{} - RDV du patient", patientId);
        List<RendezVousDTO> rendezVousList = service.getRendezVousByPatient(patientId);
        return ResponseEntity.ok(rendezVousList);
    }

    /**
     * Récupérer les rendez-vous d'un médecin pour une date.
     * GET /api/rendezvous/medecin/{medecinId}/date/{date}
     */
    @GetMapping("/medecin/{medecinId}/date/{date}")
    public ResponseEntity<List<RendezVousDTO>> getRendezVousByMedecinAndDate(
            @PathVariable Long medecinId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("🔍 GET /api/rendezvous/medecin/{}/date/{} - RDV du médecin", medecinId, date);
        List<RendezVousDTO> rendezVousList = service.getRendezVousByMedecinAndDate(medecinId, date);
        return ResponseEntity.ok(rendezVousList);
    }

    /**
     * Récupérer les rendez-vous du jour d'un médecin.
     * GET /api/rendezvous/medecin/{medecinId}/aujourdhui
     */
    @GetMapping("/medecin/{medecinId}/aujourdhui")
    public ResponseEntity<List<RendezVousDTO>> getRendezVousDuJour(
            @PathVariable Long medecinId) {
        log.info("📅 GET /api/rendezvous/medecin/{}/aujourdhui - RDV du jour", medecinId);
        List<RendezVousDTO> rendezVousList = service.getRendezVousDuJour(medecinId);
        return ResponseEntity.ok(rendezVousList);
    }

    // ========== Disponibilités ==========

    /**
     * Récupérer les créneaux disponibles d'un médecin.
     * GET /api/rendezvous/medecin/{medecinId}/disponibilites?date=2025-01-20
     */
    @GetMapping("/medecin/{medecinId}/disponibilites")
    public ResponseEntity<DisponibilitesDTO> getDisponibilites(
            @PathVariable Long medecinId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("📋 GET /api/rendezvous/medecin/{}/disponibilites?date={}", medecinId, date);
        DisponibilitesDTO disponibilites = service.getDisponibilites(medecinId, date);
        return ResponseEntity.ok(disponibilites);
    }

    // ========== Liste d'Attente ==========

    /**
     * Récupérer la liste d'attente d'un médecin.
     * GET /api/rendezvous/liste-attente/medecin/{medecinId}
     */
    @GetMapping("/liste-attente/medecin/{medecinId}")
    public ResponseEntity<List<RendezVousDTO>> getListeAttente(
            @PathVariable Long medecinId) {
        log.info("📝 GET /api/rendezvous/liste-attente/medecin/{} - Liste d'attente", medecinId);
        List<RendezVousDTO> listeAttente = service.getListeAttente(medecinId);
        return ResponseEntity.ok(listeAttente);
    }

    /**
     * Ajouter un patient à la liste d'attente.
     * POST /api/rendezvous/{id}/ajouter-attente
     */
    @PostMapping("/{id}/ajouter-attente")
    public ResponseEntity<RendezVousDTO> ajouterEnListeAttente(@PathVariable Long id) {
        log.info("➕ POST /api/rendezvous/{}/ajouter-attente - Ajout à la liste d'attente", id);
        RendezVousDTO updated = service.ajouterEnListeAttente(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Retirer un patient de la liste d'attente.
     * POST /api/rendezvous/{id}/retirer-attente
     */
    @PostMapping("/{id}/retirer-attente")
    public ResponseEntity<RendezVousDTO> retirerDeListeAttente(@PathVariable Long id) {
        log.info("➖ POST /api/rendezvous/{}/retirer-attente - Retrait de la liste d'attente", id);
        RendezVousDTO updated = service.retirerDeListeAttente(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Récupérer le patient suivant dans la file d'attente.
     * GET /api/rendezvous/liste-attente/suivant/{medecinId}
     */
    @GetMapping("/liste-attente/suivant/{medecinId}")
    public ResponseEntity<RendezVousDTO> getPatientSuivant(@PathVariable Long medecinId) {
        log.info("👤 GET /api/rendezvous/liste-attente/suivant/{} - Patient suivant", medecinId);
        RendezVousDTO patientSuivant = service.getPatientSuivant(medecinId);
        return ResponseEntity.ok(patientSuivant);
    }

    // ========== Health Check ==========

    /**
     * Vérifier l'état du service.
     * GET /api/rendezvous/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("✅ RendezVous Service is UP and running!");
    }
}