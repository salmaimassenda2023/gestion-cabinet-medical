package com.example.rendezvousservice.client.patient;



import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Client Feign pour communiquer avec le microservice Patient.
 *
 * APPROCHE SIMPLE ET FLEXIBLE :
 * - getPatientInfo() : Infos basiques (rapide, pour les listes)
 * - getDossierMedical() : Dossier médical complet (pour les notifications)
 */
@FeignClient(
        name = "patient-service",
        url = "http://localhost:8084",  
        fallback = PatientClientFallback.class
)
public interface PatientClient {


    @GetMapping("/api/patient/{patientId}")
    PatientInfoDTO getPatientInfo(@PathVariable("patientId") Long patientId);

    /**
     *  Récupère le dossier médical d'un patient.
     * Utilisé pour : notifications au médecin, consultations
     *
     * Endpoint: GET /api/patient/{patientId}/dossier
     */
    @GetMapping("/api/patient/{patientId}/dossier")
    DossierMedicalDTO getDossierMedical(@PathVariable("patientId") Long patientId);
}
