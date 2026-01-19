package com.example.consultationservice.controller;

import com.example.consultationservice.dto.*;
import com.example.consultationservice.exception.ServiceValidationException;
import com.example.consultationservice.service.ConsultationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour la gestion des consultations médicales
 * Responsabilité: Exposer les endpoints API et déléguer au service
 */
@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
@Slf4j
public class ConsultationController {

    private final ConsultationService consultationService;

    /**
     * Créer une nouvelle consultation
     */
    @PostMapping
    public ResponseEntity<ConsultationResponseDTO> createConsultation(
            @Valid @RequestBody ConsultationCreateDTO dto) throws ServiceValidationException {
        log.info("Création d'une consultation pour le patient ID: {}", dto.getIdPatient());
        ConsultationResponseDTO response = consultationService.createConsultation(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer une consultation par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> getConsultation(@PathVariable Long id) {
        log.info("Récupération de la consultation ID: {}", id);
        ConsultationResponseDTO response = consultationService.getConsultation(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour une consultation
     */
    @PutMapping("/{id}")
    public ResponseEntity<ConsultationResponseDTO> updateConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationUpdateDTO dto) {
        log.info("Mise à jour de la consultation ID: {}", id);
        ConsultationResponseDTO response = consultationService.updateConsultation(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une consultation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConsultation(@PathVariable Long id) {
        log.info("Suppression de la consultation ID: {}", id);
        consultationService.deleteConsultation(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Récupérer toutes les consultations d'un patient
     */
    @GetMapping("/patient/{idPatient}")
    public ResponseEntity<List<ConsultationResponseDTO>> getConsultationsByPatient(
            @PathVariable Long idPatient) {
        log.info("Récupération des consultations du patient ID: {}", idPatient);
        List<ConsultationResponseDTO> response = consultationService.getConsultationsByPatient(idPatient);
        return ResponseEntity.ok(response);
    }

    /**
     * Rechercher des consultations par critères
     */
    @GetMapping("/search")
    public ResponseEntity<List<ConsultationResponseDTO>> searchConsultations(
            @RequestParam(required = false) Long idPatient,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {
        log.info("Recherche de consultations avec filtres");
        List<ConsultationResponseDTO> response = consultationService.searchConsultations(
                idPatient, dateDebut, dateFin);
        return ResponseEntity.ok(response);
    }

    // ============ Examens Cliniques ============

    /**
     * Ajouter un examen clinique à une consultation
     */
    @PostMapping("/{idConsultation}/examens")
    public ResponseEntity<ExamenCliniqueDTO> addExamenClinique(
            @PathVariable Long idConsultation,
            @Valid @RequestBody ExamenCliniqueCreateDTO dto) {
        log.info("Ajout d'examen clinique à la consultation ID: {}", idConsultation);
        ExamenCliniqueDTO response = consultationService.addExamenClinique(idConsultation, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer tous les examens d'une consultation
     */
    @GetMapping("/{idConsultation}/examens")
    public ResponseEntity<List<ExamenCliniqueDTO>> getExamensCliniques(
            @PathVariable Long idConsultation) {
        log.info("Récupération des examens de la consultation ID: {}", idConsultation);
        List<ExamenCliniqueDTO> response = consultationService.getExamensCliniques(idConsultation);
        return ResponseEntity.ok(response);
    }

    // ============ Ordonnances Médicaments ============

    /**
     * Créer une ordonnance de médicaments
     */
    @PostMapping("/{idConsultation}/ordonnances/medicaments")
    public ResponseEntity<OrdonnanceMedicamentDTO> createOrdonnanceMedicament(
            @PathVariable Long idConsultation,
            @Valid @RequestBody OrdonnanceMedicamentCreateDTO dto) {
        log.info("Création d'ordonnance médicament pour consultation ID: {}", idConsultation);
        OrdonnanceMedicamentDTO response = consultationService.createOrdonnanceMedicament(
                idConsultation, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Télécharger l'ordonnance de médicaments en PDF
     */
    @GetMapping("/{idConsultation}/ordonnances/medicaments/{idOrdonnance}/pdf")
    public ResponseEntity<byte[]> downloadOrdonnanceMedicamentPDF(
            @PathVariable Long idConsultation,
            @PathVariable Long idOrdonnance) {
        log.info("Génération PDF ordonnance médicament ID: {}", idOrdonnance);
        byte[] pdf = consultationService.generateOrdonnanceMedicamentPDF(idOrdonnance);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ordonnance_medicament_" + idOrdonnance + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ============ Ordonnances Examens ============

    /**
     * Créer une ordonnance d'examens supplémentaires
     */
    @PostMapping("/{idConsultation}/ordonnances/examens")
    public ResponseEntity<OrdonnanceExamenDTO> createOrdonnanceExamen(
            @PathVariable Long idConsultation,
            @Valid @RequestBody OrdonnanceExamenCreateDTO dto) {
        log.info("Création d'ordonnance examen pour consultation ID: {}", idConsultation);
        OrdonnanceExamenDTO response = consultationService.createOrdonnanceExamen(
                idConsultation, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Télécharger l'ordonnance d'examens en PDF
     */
    @GetMapping("/{idConsultation}/ordonnances/examens/{idOrdonnance}/pdf")
    public ResponseEntity<byte[]> downloadOrdonnanceExamenPDF(
            @PathVariable Long idConsultation,
            @PathVariable Long idOrdonnance) {
        log.info("Génération PDF ordonnance examen ID: {}", idOrdonnance);
        byte[] pdf = consultationService.generateOrdonnanceExamenPDF(idOrdonnance);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "ordonnance_examen_" + idOrdonnance + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    // ============ Factures ============

    /**
     * Créer/Générer une facture pour la consultation
     */
    @PostMapping("/{consultationId}/factures")
    public ResponseEntity<FactureDTO> createFacture(
            @PathVariable Long consultationId,
            @Valid @RequestBody FactureCreateDTO dto) throws ServiceValidationException {
        log.info("Création de facture pour consultation ID: {}", consultationId);
        FactureDTO response = consultationService.createFacture(consultationId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer la facture d'une consultation
     */
    @GetMapping("/{idConsultation}/factures")
    public ResponseEntity<List<FactureDTO>> getFactures(@PathVariable Long idConsultation) {
        log.info("Récupération des factures de la consultation ID: {}", idConsultation);
        List<FactureDTO> response = consultationService.getFactures(idConsultation);
        return ResponseEntity.ok(response);
    }

    /**
     * Télécharger la facture en PDF
     */
    @GetMapping("/factures/{idFacture}/pdf")
    public ResponseEntity<byte[]> downloadFacturePDF(@PathVariable Long idFacture) {
        log.info("Génération PDF facture ID: {}", idFacture);
        byte[] pdf = consultationService.generateFacturePDF(idFacture);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "facture_" + idFacture + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    /**
     * Mettre à jour le statut d'une facture
     */
    @PatchMapping("/factures/{idFacture}/statut")
    public ResponseEntity<FactureDTO> updateFactureStatut(
            @PathVariable Long idFacture,
            @RequestParam String statut) {
        log.info("Mise à jour statut facture ID: {} -> {}", idFacture, statut);
        FactureDTO response = consultationService.updateFactureStatut(idFacture, statut);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/cabinet/{cabinetId}/factures")
    public ResponseEntity<List<FactureDTO>> getAllFacturesByCabinet(
            @PathVariable Long cabinetId) {
        log.info("Récupération des factures du cabinet ID: {}", cabinetId);
        List<FactureDTO> factures = consultationService.getAllFacturesByCabinetWithDetails(cabinetId);
        return ResponseEntity.ok(factures);
    }


}