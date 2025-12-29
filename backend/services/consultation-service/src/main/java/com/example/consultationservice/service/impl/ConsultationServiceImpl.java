package com.example.consultationservice.service.impl;

import com.example.consultationservice.client.CabinetServiceClient;
import com.example.consultationservice.client.PatientServiceClient;
import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import com.example.consultationservice.exception.ResourceNotFoundException;
import com.example.consultationservice.exception.ServiceValidationException;
import com.example.consultationservice.mapper.*;
import com.example.consultationservice.repository.*;
import com.example.consultationservice.service.ConsultationService;
import com.example.consultationservice.service.PDFGeneratorService;
import com.example.consultationservice.dto.ConsultationPatientResponseDTO;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Implémentation du service de consultation
 * Responsabilité: Orchestrer la logique métier et coordonner les repositories
 * Principe SOLID: Single Responsibility - chaque méthode a un objectif clair
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final ExamenCliniqueRepository examenCliniqueRepository;
    private final OrdonnanceMedicamentRepository ordonnanceMedicamentRepository;
    private final OrdonnanceExamenRepository ordonnanceExamenRepository;
    private final FactureRepository factureRepository;
    private final CabinetServiceClient cabinetServiceClient;

    private final ConsultationMapper consultationMapper;
    private final ExamenCliniqueMapper examenCliniqueMapper;
    private final OrdonnanceMedicamentMapper ordonnanceMedicamentMapper;
    private final OrdonnanceExamenMapper ordonnanceExamenMapper;
    private final FactureMapper factureMapper;

    private final PatientServiceClient patientServiceClient;
    private final PDFGeneratorService pdfGeneratorService;


    @Override
    @Transactional
    public ConsultationResponseDTO createConsultation(ConsultationCreateDTO dto) throws ServiceValidationException {
        log.info("=== Début de création de consultation ===");

        // STEP 1: Validate Patient Exists
        ConsultationPatientResponseDTO patient = validateAndGetPatient(dto.getIdPatient());

        // STEP 2: Validate Cabinet Exists
        validateCabinet(dto.getIdCabinet());

        // STEP 3: Create Consultation Entity
        Consultation consultation = consultationMapper.toEntity(dto);
        consultation.setDateConsultation(new Date());
        consultation.setConsultationServices(new ArrayList<>());  // Updated

        // STEP 4: Process Each Service
        List<ConsultationServiceItem> consultationServices = new ArrayList<>();
        double montantTotal = 0.0;

        for (ConsultationServiceItemDTO serviceDTO : dto.getServices()) {
            // Fetch service details
            ServiceConsultationDTO serviceDetails = validateAndGetService(
                    dto.getIdCabinet(),
                    serviceDTO.getIdService()
            );

            // Create ConsultationServiceItem entity
            ConsultationServiceItem consultationService = ConsultationServiceItem.builder()
                    .consultation(consultation)
                    .idService(serviceDTO.getIdService())
                    .nomService(serviceDetails.getNomService())
                    .prix(serviceDetails.getPrix())
                    .build();

            consultationServices.add(consultationService);
            montantTotal += serviceDetails.getPrix();
        }

        consultation.setConsultationServices(consultationServices);  // Updated
        consultation.setMontantTotal(montantTotal);  // Set total

        // STEP 5: Save Consultation
        Consultation savedConsultation = consultationRepository.save(consultation);

        // STEP 6: Return Response
        return consultationMapper.toDTO(savedConsultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponseDTO getConsultation(Long id) {
        Consultation consultation = findConsultationById(id);
        return consultationMapper.toDTO(consultation);
    }

    @Override
    public ConsultationResponseDTO updateConsultation(Long id, ConsultationUpdateDTO dto) {
        Consultation consultation = findConsultationById(id);

        if (dto.getDiagnostic() != null) {
            consultation.setDiagnostic(dto.getDiagnostic());
        }

        Consultation updated = consultationRepository.save(consultation);
        log.info("Consultation {} mise à jour", id);

        return consultationMapper.toDTO(updated);
    }

    @Override
    public void deleteConsultation(Long id) {
        Consultation consultation = findConsultationById(id);
        consultationRepository.delete(consultation);
        log.info("Consultation {} supprimée", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> getConsultationsByPatient(Long idPatient) {
        List<Consultation> consultations = consultationRepository.findByIdPatientOrderByDateConsultationDesc(idPatient);
        return consultations.stream()
                .map(consultationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultationResponseDTO> searchConsultations(Long idPatient, String dateDebut, String dateFin) {
        List<Consultation> consultations;

        if (idPatient != null && dateDebut != null && dateFin != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date debut = sdf.parse(dateDebut);
                Date fin = sdf.parse(dateFin);
                consultations = consultationRepository.findByIdPatientAndDateConsultationBetween(
                        idPatient, debut, fin);
            } catch (ParseException e) {
                throw new IllegalArgumentException("Format de date invalide. Utilisez yyyy-MM-dd");
            }
        } else if (idPatient != null) {
            consultations = consultationRepository.findByIdPatientOrderByDateConsultationDesc(idPatient);
        } else {
            consultations = consultationRepository.findAll();
        }

        return consultations.stream()
                .map(consultationMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ============ Examens Cliniques ============

    @Override
    public ExamenCliniqueDTO addExamenClinique(Long idConsultation, ExamenCliniqueCreateDTO dto) {
        Consultation consultation = findConsultationById(idConsultation);

        ExamenClinique examen = examenCliniqueMapper.toEntity(dto);
        examen.setConsultation(consultation);

        ExamenClinique saved = examenCliniqueRepository.save(examen);
        log.info("Examen clinique ajouté: {} = {} {}",
                saved.getTypeExamen(), saved.getValeur(), saved.getUnite());

        return examenCliniqueMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamenCliniqueDTO> getExamensCliniques(Long idConsultation) {
        Consultation consultation = findConsultationById(idConsultation);
        return consultation.getExamensCliniques().stream()
                .map(examenCliniqueMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ============ Ordonnances Médicaments ============

    @Override
    public OrdonnanceMedicamentDTO createOrdonnanceMedicament(Long idConsultation, OrdonnanceMedicamentCreateDTO dto) {
        Consultation consultation = findConsultationById(idConsultation);

        OrdonnanceMedicament ordonnance = ordonnanceMedicamentMapper.toEntity(dto);
        ordonnance.setConsultation(consultation);
        ordonnance.setDateCreation(new Date());

        // Associer les lignes à l'ordonnance
        if (ordonnance.getLignes() != null) {
            ordonnance.getLignes().forEach(ligne -> ligne.setOrdonnanceMedicament(ordonnance));
        }

        OrdonnanceMedicament saved = ordonnanceMedicamentRepository.save(ordonnance);
        log.info("Ordonnance médicament créée: ID {}", saved.getIdOrdonnance());

        return ordonnanceMedicamentMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateOrdonnanceMedicamentPDF(Long idOrdonnance) {
        OrdonnanceMedicament ordonnance = ordonnanceMedicamentRepository.findById(idOrdonnance)
                .orElseThrow(() -> new ResourceNotFoundException("Ordonnance médicament non trouvée: " + idOrdonnance));

        // Récupérer les infos du patient
        ConsultationPatientResponseDTO patient = patientServiceClient
                .getPatient(ordonnance.getConsultation().getIdPatient());

        return pdfGeneratorService.generateOrdonnanceMedicamentPDF(ordonnance, patient);
    }

    // ============ Ordonnances Examens ============

    @Override
    public OrdonnanceExamenDTO createOrdonnanceExamen(Long idConsultation, OrdonnanceExamenCreateDTO dto) {
        Consultation consultation = findConsultationById(idConsultation);

        OrdonnanceExamen ordonnance = ordonnanceExamenMapper.toEntity(dto);
        ordonnance.setConsultation(consultation);
        ordonnance.setDateCreation(new Date());

        // Associer les lignes à l'ordonnance
        if (ordonnance.getLignes() != null) {
            ordonnance.getLignes().forEach(ligne -> ligne.setOrdonnanceExamen(ordonnance));
        }

        OrdonnanceExamen saved = ordonnanceExamenRepository.save(ordonnance);
        log.info("Ordonnance examen créée: ID {}", saved.getIdOrdonnance());

        return ordonnanceExamenMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateOrdonnanceExamenPDF(Long idOrdonnance) {
        OrdonnanceExamen ordonnance = ordonnanceExamenRepository.findById(idOrdonnance)
                .orElseThrow(() -> new ResourceNotFoundException("Ordonnance examen non trouvée: " + idOrdonnance));

        // Récupérer les infos du patient
        ConsultationPatientResponseDTO patient = patientServiceClient
                .getPatient(ordonnance.getConsultation().getIdPatient());

        return pdfGeneratorService.generateOrdonnanceExamenPDF(ordonnance, patient);
    }

    // ============ Factures ============

    @Override
    public FactureDTO createFacture(Long consultationId, FactureCreateDTO dto) throws ServiceValidationException {
        log.info("🧾 Création de facture pour consultation ID: {}", consultationId);

        // 1. Find the consultation with all its services
        Consultation consultation = findConsultationById(consultationId);

        // 2. Verify consultation has services
        if (consultation.getConsultationServices() == null ||
                consultation.getConsultationServices().isEmpty()) {
            log.error("❌ Consultation {} n'a aucun service", consultationId);
            throw new ServiceValidationException(
                    "Impossible de créer une facture pour une consultation sans services");
        }

        // 3. Use the total amount already calculated in the consultation
        Double montantTotal = consultation.getMontantTotal();

        if (montantTotal == null || montantTotal <= 0) {
            log.error("❌ Montant total invalide pour consultation {}", consultationId);
            throw new ServiceValidationException(
                    "Le montant total de la consultation est invalide");
        }

        log.info("📋 Consultation trouvée avec {} service(s) - Montant total: {} MAD",
                consultation.getConsultationServices().size(), montantTotal);

        // 4. Create facture for the entire consultation
        Facture facture = Facture.builder()
                .consultation(consultation)
                .cabinetId(consultation.getIdCabinet())
                .montantTotal(montantTotal)
                .dateFacture(new Date())
                .statut(dto.getStatut() != null ? dto.getStatut() : "EN_ATTENTE")
                .notes(dto.getNotes())
                .build();

        Facture saved = factureRepository.save(facture);

        log.info("✅ Facture créée avec succès:");
        log.info("   - ID Facture: {}", saved.getIdFacture());
        log.info("   - Consultation: {}", consultationId);
        log.info("   - Montant total: {} MAD", saved.getMontantTotal());
        log.info("   - Nombre de services: {}", consultation.getConsultationServices().size());
        log.info("   - Statut: {}", saved.getStatut());

        return factureMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactureDTO> getFactures(Long idConsultation) {
        Consultation consultation = findConsultationById(idConsultation);
        return consultation.getFactures().stream()
                .map(factureMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateFacturePDF(Long idFacture) {
        Facture facture = factureRepository.findById(idFacture)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée: " + idFacture));

        // Récupérer les infos du patient
        ConsultationPatientResponseDTO patient = patientServiceClient.getPatient(facture.getConsultation().getIdPatient());

        return pdfGeneratorService.generateFacturePDF(facture, patient);
    }

    @Override
    public FactureDTO updateFactureStatut(Long idFacture, String statut) {
        Facture facture = factureRepository.findById(idFacture)
                .orElseThrow(() -> new ResourceNotFoundException("Facture non trouvée: " + idFacture));

        facture.setStatut(statut);
        Facture updated = factureRepository.save(facture);
        log.info("Statut facture {} mis à jour: {}", idFacture, statut);

        return factureMapper.toDTO(updated);
    }


    // ============ Méthodes utilitaires privées ============

    private Consultation findConsultationById(Long id) {
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation non trouvée avec l'ID: " + id));
    }
    /**
     * Validates that the patient exists by calling patient-service
     */
    private ConsultationPatientResponseDTO validateAndGetPatient(Long idPatient) throws ServiceValidationException {
        log.debug("Validation du patient ID: {}", idPatient);

        try {
            ConsultationPatientResponseDTO patient = patientServiceClient.getPatient(idPatient);

            if (patient == null) {
                throw new ResourceNotFoundException(
                        "Patient non trouvé avec l'ID: " + idPatient);
            }

            return patient;

        } catch (FeignException.NotFound e) {
            log.error("Patient non trouvé: ID {}", idPatient);
            throw new ResourceNotFoundException(
                    "Patient non trouvé avec l'ID: " + idPatient);

        } catch (FeignException e) {
            log.error("Erreur lors de la communication avec patient-service: {}",
                    e.getMessage());
            throw new ServiceValidationException(
                    "Impossible de valider le patient. Service patient indisponible.");
        }
    }

    /**
     * Validates that the cabinet exists (optional method)
     */
    private void validateCabinet(Long idCabinet) throws ServiceValidationException {
        log.debug("Validation du cabinet ID: {}", idCabinet);

        try {
            // Try to get services list to verify cabinet exists
            List<ServiceConsultationDTO> services = cabinetServiceClient.getServices(idCabinet);

            if (services == null) {
                throw new ResourceNotFoundException(
                        "Cabinet non trouvé avec l'ID: " + idCabinet);
            }

        } catch (FeignException.NotFound e) {
            log.error("Cabinet non trouvé: ID {}", idCabinet);
            throw new ResourceNotFoundException(
                    "Cabinet non trouvé avec l'ID: " + idCabinet);

        } catch (FeignException e) {
            log.error("Erreur lors de la communication avec cabinet-service: {}",
                    e.getMessage());
            throw new ServiceValidationException(
                    "Impossible de valider le cabinet. Service cabinet indisponible.");
        }
    }

    /**
     * Validates and retrieves service details from cabinet-service
     */
    private ServiceConsultationDTO validateAndGetService(Long idCabinet, Long idService) throws ServiceValidationException {
        log.debug("Récupération du service ID: {} du cabinet ID: {}", idService, idCabinet);

        try {
            ServiceConsultationDTO service = cabinetServiceClient
                    .getServiceById(idCabinet, idService);

            if (service == null) {
                throw new ResourceNotFoundException(
                        String.format("Service avec l'ID %d non trouvé au cabinet %d",
                                idService, idCabinet));
            }

            // Validate service price
            if (service.getPrix() == null || service.getPrix() <= 0) {
                throw new ServiceValidationException(
                        String.format("Le service %d a un prix invalide", idService));
            }

            return service;

        } catch (FeignException.NotFound e) {
            log.error("Service non trouvé: ID {} au cabinet ID {}", idService, idCabinet);
            throw new ResourceNotFoundException(
                    String.format("Service avec l'ID %d non trouvé au cabinet %d",
                            idService, idCabinet));

        } catch (FeignException e) {
            log.error("Erreur lors de la récupération du service {}: {}",
                    idService, e.getMessage());
            throw new ServiceValidationException(
                    String.format("Impossible de récupérer le service %d. " +
                            "Service cabinet indisponible.", idService));
        }
    }

    /**
     * Creates a ConsultationService entity with cached service details
     */
    private ConsultationServiceItem createConsultationService(  // Updated
                                                                Consultation consultation,
                                                                ServiceConsultationDTO serviceDTO,
                                                                ServiceConsultationDTO serviceDetails) {

        ConsultationServiceItem consultationService = ConsultationServiceItem.builder()  // Updated
                .consultation(consultation)
                .idService(serviceDTO.getIdService())
                .nomService(serviceDetails.getNomService())
                .prix(serviceDetails.getPrix())
                .build();

        return consultationService;
    }

}