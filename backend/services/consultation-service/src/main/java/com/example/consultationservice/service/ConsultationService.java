package com.example.consultationservice.service;

import com.example.consultationservice.dto.*;
import com.example.consultationservice.exception.ServiceValidationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface ConsultationService {

    // ============ Gestion des Consultations ============

    ConsultationResponseDTO createConsultation(ConsultationCreateDTO dto) throws ServiceValidationException;

   
    @Transactional(readOnly = true)
    ConsultationResponseDTO getConsultation(Long id);

    
    ConsultationResponseDTO updateConsultation(Long id, ConsultationUpdateDTO dto);

   
    void deleteConsultation(Long id);

    
    @Transactional(readOnly = true)
    List<ConsultationResponseDTO> getConsultationsByPatient(Long idPatient);

   
    @Transactional(readOnly = true)
    List<ConsultationResponseDTO> searchConsultations(Long idPatient, String dateDebut, String dateFin);

    // ============ Gestion des Examens Cliniques ============

   
    ExamenCliniqueDTO addExamenClinique(Long idConsultation, ExamenCliniqueCreateDTO dto);

    
    @Transactional(readOnly = true)
    List<ExamenCliniqueDTO> getExamensCliniques(Long idConsultation);

    // ============ Gestion des Ordonnances de Médicaments ============

    
    OrdonnanceMedicamentDTO createOrdonnanceMedicament(Long idConsultation, OrdonnanceMedicamentCreateDTO dto);

    @Transactional(readOnly = true)
    byte[] generateOrdonnanceMedicamentPDF(Long idOrdonnance);

    // ============ Gestion des Ordonnances d'Examens ============

   
    OrdonnanceExamenDTO createOrdonnanceExamen(Long idConsultation, OrdonnanceExamenCreateDTO dto);

    
    @Transactional(readOnly = true)
    byte[] generateOrdonnanceExamenPDF(Long idOrdonnance);

    // ============ Gestion des Factures ============

    FactureDTO createFacture(Long idCabinet, FactureCreateDTO dto) throws ServiceValidationException;

    
    @Transactional(readOnly = true)
    List<FactureDTO> getFactures(Long idConsultation);

    
    @Transactional(readOnly = true)
    byte[] generateFacturePDF(Long idFacture);

   
    FactureDTO updateFactureStatut(Long idFacture, String statut);
    
    List<FactureDTO> getAllFacturesByCabinet(Long cabinetId);

    List<FactureDTO> getAllFacturesByCabinetWithDetails(Long cabinetId);
}