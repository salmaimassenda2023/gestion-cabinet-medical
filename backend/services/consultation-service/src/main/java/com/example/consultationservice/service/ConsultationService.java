package com.example.consultationservice.service;

import com.example.consultationservice.dto.*;
import com.example.consultationservice.exception.ServiceValidationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Interface pour le service de consultation
 * Définit le contrat pour la gestion des consultations et leurs composants
 */
public interface ConsultationService {

    // ============ Gestion des Consultations ============

    /**
     * Crée une nouvelle consultation
     * 
     * @param dto DTO contenant les données de la consultation
     * @return Consultation créée
     */
    ConsultationResponseDTO createConsultation(ConsultationCreateDTO dto) throws ServiceValidationException;

    /**
     * Récupère une consultation par son ID
     * 
     * @param id ID de la consultation
     * @return Consultation trouvée
     */
    @Transactional(readOnly = true)
    ConsultationResponseDTO getConsultation(Long id);

    /**
     * Met à jour une consultation existante
     * 
     * @param id  ID de la consultation à mettre à jour
     * @param dto DTO contenant les données à mettre à jour
     * @return Consultation mise à jour
     */
    ConsultationResponseDTO updateConsultation(Long id, ConsultationUpdateDTO dto);

    /**
     * Supprime une consultation
     * 
     * @param id ID de la consultation à supprimer
     */
    void deleteConsultation(Long id);

    /**
     * Récupère toutes les consultations d'un patient
     * 
     * @param idPatient ID du patient
     * @return Liste des consultations du patient
     */
    @Transactional(readOnly = true)
    List<ConsultationResponseDTO> getConsultationsByPatient(Long idPatient);

    /**
     * Recherche des consultations avec filtres
     * 
     * @param idPatient ID du patient (optionnel)
     * @param dateDebut Date de début (format yyyy-MM-dd) (optionnel)
     * @param dateFin   Date de fin (format yyyy-MM-dd) (optionnel)
     * @return Liste des consultations correspondant aux critères
     */
    @Transactional(readOnly = true)
    List<ConsultationResponseDTO> searchConsultations(Long idPatient, String dateDebut, String dateFin);

    // ============ Gestion des Examens Cliniques ============

    /**
     * Ajoute un examen clinique à une consultation
     * 
     * @param idConsultation ID de la consultation
     * @param dto            DTO contenant les données de l'examen clinique
     * @return Examen clinique créé
     */
    ExamenCliniqueDTO addExamenClinique(Long idConsultation, ExamenCliniqueCreateDTO dto);

    /**
     * Récupère tous les examens cliniques d'une consultation
     * 
     * @param idConsultation ID de la consultation
     * @return Liste des examens cliniques
     */
    @Transactional(readOnly = true)
    List<ExamenCliniqueDTO> getExamensCliniques(Long idConsultation);

    // ============ Gestion des Ordonnances de Médicaments ============

    /**
     * Crée une ordonnance de médicaments pour une consultation
     *
     * @param idConsultation ID de la consultation
     * @param dto            DTO contenant les données de l'ordonnance
     * @return Ordonnance créée
     */
    OrdonnanceMedicamentDTO createOrdonnanceMedicament(Long idConsultation, OrdonnanceMedicamentCreateDTO dto);

    /**
     * Génère un PDF pour une ordonnance de médicaments
     * 
     * @param idOrdonnance ID de l'ordonnance
     * @return PDF sous forme de tableau d'octets
     */
    @Transactional(readOnly = true)
    byte[] generateOrdonnanceMedicamentPDF(Long idOrdonnance);

    // ============ Gestion des Ordonnances d'Examens ============

    /**
     * Crée une ordonnance d'examens pour une consultation
     * 
     * @param idConsultation ID de la consultation
     * @param dto            DTO contenant les données de l'ordonnance
     * @return Ordonnance créée
     */
    OrdonnanceExamenDTO createOrdonnanceExamen(Long idConsultation, OrdonnanceExamenCreateDTO dto);

    /**
     * Génère un PDF pour une ordonnance d'examens
     * 
     * @param idOrdonnance ID de l'ordonnance
     * @return PDF sous forme de tableau d'octets
     */
    @Transactional(readOnly = true)
    byte[] generateOrdonnanceExamenPDF(Long idOrdonnance);

    // ============ Gestion des Factures ============

    FactureDTO createFacture(Long idCabinet, FactureCreateDTO dto) throws ServiceValidationException;

    /**
     * Récupère toutes les factures d'une consultation
     * 
     * @param idConsultation ID de la consultation
     * @return Liste des factures
     */
    @Transactional(readOnly = true)
    List<FactureDTO> getFactures(Long idConsultation);

    /**
     * Génère un PDF pour une facture
     * 
     * @param idFacture ID de la facture
     * @return PDF sous forme de tableau d'octets
     */
    @Transactional(readOnly = true)
    byte[] generateFacturePDF(Long idFacture);

    /**
     * Met à jour le statut d'une facture
     * 
     * @param idFacture ID de la facture
     * @param statut    Nouveau statut
     * @return Facture mise à jour
     */
    FactureDTO updateFactureStatut(Long idFacture, String statut);

}