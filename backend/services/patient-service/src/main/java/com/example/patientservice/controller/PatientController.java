package com.example.patientservice.controller;

import com.example.patientservice.dto.*;
import com.example.patientservice.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<PatientResponseDTO> createPatient(@Valid @RequestBody PatientCreateDTO dto) {
        log.info(" Requête de création de patient reçue");
        PatientResponseDTO response = patientService.createPatient(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     *  NOUVEAU ENDPOINT : Infos basiques sans dossier médical.
     * Utilisé par les autres microservices pour optimiser les performances.
     */
    @GetMapping("/{id}/info")
    public ResponseEntity<PatientInfoDTO> getPatientInfo(@PathVariable Long id) {
        log.info("️ Récupération des infos basiques du patient ID: {}", id);
        PatientInfoDTO response = patientService.getPatientInfo(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> getPatient(@PathVariable Long id) {
        log.info(" Récupération du patient ID: {}", id);
        PatientResponseDTO response = patientService.getPatient(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateDTO dto) {
        log.info("️ Mise à jour du patient ID: {}", id);
        PatientResponseDTO response = patientService.updatePatient(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        log.info(" Suppression du patient ID: {}", id);
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients() {
        log.info(" Récupération de tous les patients");
        List<PatientResponseDTO> response = patientService.getAllPatients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cabinet/{idCabinet}")
    public ResponseEntity<List<PatientResponseDTO>> getPatientsByCabinet(@PathVariable Long idCabinet) {
        log.info(" Récupération des patients du cabinet ID: {}", idCabinet);
        List<PatientResponseDTO> response = patientService.getPatientsByCabinet(idCabinet);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PatientResponseDTO>> searchPatients(@RequestParam String searchTerm) {
        log.info(" Recherche de patients: {}", searchTerm);
        List<PatientResponseDTO> response = patientService.searchPatients(searchTerm);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cabinet/{idCabinet}/search")
    public ResponseEntity<List<PatientResponseDTO>> searchPatientsByCabinet(
            @PathVariable Long idCabinet,
            @RequestParam String searchTerm) {
        log.info(" Recherche de patients dans le cabinet {}: {}", idCabinet, searchTerm);
        List<PatientResponseDTO> response = patientService.searchPatientsByCabinet(idCabinet, searchTerm);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cin/{cin}")
    public ResponseEntity<PatientResponseDTO> getPatientByCin(@PathVariable String cin) {
        log.info(" Récupération du patient par CIN: {}", cin);
        PatientResponseDTO response = patientService.getPatientByCin(cin);
        return ResponseEntity.ok(response);
    }

    // ============ Dossier Médical ============

    @GetMapping("/{patientId}/dossier")
    public ResponseEntity<DossierMedicalDTO> getDossierMedical(@PathVariable Long patientId) {
        log.info(" Récupération du dossier médical du patient: {}", patientId);
        DossierMedicalDTO response = patientService.getDossierMedical(patientId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{patientId}/dossier")
    public ResponseEntity<DossierMedicalDTO> updateDossierMedical(
            @PathVariable Long patientId,
            @Valid @RequestBody DossierMedicalDTO dto) {
        log.info("️ Mise à jour du dossier médical du patient: {}", patientId);
        DossierMedicalDTO response = patientService.updateDossierMedical(patientId, dto);
        return ResponseEntity.ok(response);
    }

    // ============ Documents Médicaux ============

    @PostMapping(value = "/{patientId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentMedicalDTO> uploadDocument(
            @PathVariable Long patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type
            ) {
        log.info(" Upload de document pour le patient: {}", patientId);
        DocumentMedicalDTO response = patientService.uploadDocument(patientId, file, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{patientId}/documents")
    public ResponseEntity<List<DocumentMedicalDTO>> getDocuments(@PathVariable Long patientId) {
        log.info(" Récupération des documents du patient: {}", patientId);
        List<DocumentMedicalDTO> response = patientService.getDocuments(patientId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long documentId) {
        log.info("🗑                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                Suppression du document ID: {}", documentId);
        patientService.deleteDocument(documentId);
        return ResponseEntity.noContent().build();
    }
}