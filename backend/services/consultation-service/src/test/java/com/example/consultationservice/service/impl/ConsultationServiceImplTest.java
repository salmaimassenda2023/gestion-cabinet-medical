package com.example.consultationservice.service.impl;

import com.example.consultationservice.client.CabinetServiceClient;
import com.example.consultationservice.client.PatientServiceClient;
import com.example.consultationservice.dto.*;
import com.example.consultationservice.entity.*;
import com.example.consultationservice.enums.TypeExamenClinique;
import com.example.consultationservice.enums.TypeExamenSupplementaire;
import com.example.consultationservice.exception.ResourceNotFoundException;
import com.example.consultationservice.exception.ServiceValidationException;
import com.example.consultationservice.mapper.*;
import com.example.consultationservice.repository.*;
import com.example.consultationservice.service.PDFGeneratorService;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceImplTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private ExamenCliniqueRepository examenCliniqueRepository;

    @Mock
    private OrdonnanceMedicamentRepository ordonnanceMedicamentRepository;

    @Mock
    private OrdonnanceExamenRepository ordonnanceExamenRepository;

    @Mock
    private FactureRepository factureRepository;

    @Mock
    private CabinetServiceClient cabinetServiceClient;

    @Mock
    private PatientServiceClient patientServiceClient;

    @Mock
    private ConsultationMapper consultationMapper;

    @Mock
    private ExamenCliniqueMapper examenCliniqueMapper;

    @Mock
    private OrdonnanceMedicamentMapper ordonnanceMedicamentMapper;

    @Mock
    private OrdonnanceExamenMapper ordonnanceExamenMapper;

    @Mock
    private FactureMapper factureMapper;

    @Mock
    private PDFGeneratorService pdfGeneratorService;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private Consultation consultation;
    private ConsultationCreateDTO consultationCreateDTO;
    private ConsultationPatientResponseDTO patient;
    private ServiceConsultationDTO serviceDetails;
    private ConsultationResponseDTO consultationResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup patient
        patient = ConsultationPatientResponseDTO.builder()
                .idPatient(1L)
                .nom("Dupont")
                .prenom("Jean")
                .cin("AB123456")
                .dateNaissance(new Date())
                .telephone("0612345678")
                .build();

        // Setup service details
        serviceDetails = ServiceConsultationDTO.builder()
                .idService(1L)
                .nomService("Consultation Générale")
                .prix(300.0)
                .build();

        // Setup consultation entity avec liste mutable
        consultation = Consultation.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .idCabinet(3L)
                .dateConsultation(new Date())
                .diagnostic("Grippe")
                .montantTotal(300.0)
                .consultationServices(new ArrayList<>()) 
                .build();

        // Add service to consultation
        ConsultationServiceItem serviceItem = ConsultationServiceItem.builder()
                .id(1L)
                .consultation(consultation)
                .idService(1L)
                .nomService("Consultation Générale")
                .prix(300.0)
                .build();
        consultation.getConsultationServices().add(serviceItem);

        // Setup DTOs
        ConsultationServiceItemDTO serviceItemDTO = ConsultationServiceItemDTO.builder()
                .idService(1L)
                .build();

        consultationCreateDTO = ConsultationCreateDTO.builder()
                .idPatient(1L)
                .idCabinet(3L)
                .diagnostic("Grippe")
                .services(List.of(serviceItemDTO))
                .build();

        consultationResponseDTO = ConsultationResponseDTO.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .dateConsultation(new Date())
                .diagnostic("Grippe")
                .montantTotal(300.0)
                .services(List.of(
                        ConsultationServiceItemDTO.builder()
                                .idService(1L)
                                .nomService("Consultation Générale")
                                .prix(300.0)
                                .build()
                ))
                .build();
    }

    // ============ TESTS POUR CREATE CONSULTATION ============

    @Test
    void createConsultation_Success() throws ServiceValidationException {
        // Arrange
        when(patientServiceClient.getPatient(1L)).thenReturn(patient);
        when(cabinetServiceClient.getServices(3L)).thenReturn(List.of(serviceDetails));
        when(cabinetServiceClient.getServiceById(3L, 1L)).thenReturn(serviceDetails);

        // Simuler la création d'une consultation avec date
        Consultation consultationWithDate = Consultation.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .idCabinet(3L)
                .dateConsultation(new Date())
                .diagnostic("Grippe")
                .montantTotal(300.0)
                .consultationServices(new ArrayList<>())
                .build();

        when(consultationMapper.toEntity(any(ConsultationCreateDTO.class))).thenReturn(consultationWithDate);
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultationWithDate);
        when(consultationMapper.toDTO(any(Consultation.class))).thenReturn(consultationResponseDTO);

        // Act
        ConsultationResponseDTO result = consultationService.createConsultation(consultationCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdConsultation());
        assertEquals(1L, result.getIdPatient());
        assertEquals(300.0, result.getMontantTotal());

        verify(patientServiceClient, times(1)).getPatient(1L);
        verify(cabinetServiceClient, times(1)).getServices(3L);
        verify(cabinetServiceClient, times(1)).getServiceById(3L, 1L);
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    @Test
    void createConsultation_PatientNotFound_ThrowsException() {
        // Arrange
        when(patientServiceClient.getPatient(1L))
                .thenThrow(FeignException.NotFound.class);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> consultationService.createConsultation(consultationCreateDTO));

        verify(consultationRepository, never()).save(any());
    }

    @Test
    void createConsultation_CabinetNotFound_ThrowsException() {
        // Arrange
        when(patientServiceClient.getPatient(1L)).thenReturn(patient);
        when(cabinetServiceClient.getServices(3L))
                .thenThrow(FeignException.NotFound.class);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> consultationService.createConsultation(consultationCreateDTO));

        verify(consultationRepository, never()).save(any());
    }

    // ============ TESTS POUR GET CONSULTATION ============

    @Test
    void getConsultation_Success() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationMapper.toDTO(consultation)).thenReturn(consultationResponseDTO);

        // Act
        ConsultationResponseDTO result = consultationService.getConsultation(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdConsultation());
        verify(consultationRepository, times(1)).findById(1L);
    }

    // ============ TESTS POUR UPDATE CONSULTATION ============

    @Test
    void updateConsultation_Success() {
        // Arrange
        ConsultationUpdateDTO updateDTO = ConsultationUpdateDTO.builder()
                .diagnostic("Diagnostic mis à jour")
                .build();

        Consultation updatedConsultation = Consultation.builder()
                .idConsultation(1L)
                .diagnostic("Diagnostic mis à jour")
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(updatedConsultation);
        when(consultationMapper.toDTO(any(Consultation.class))).thenReturn(
                ConsultationResponseDTO.builder()
                        .idConsultation(1L)
                        .diagnostic("Diagnostic mis à jour")
                        .build()
        );

        // Act
        ConsultationResponseDTO result = consultationService.updateConsultation(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Diagnostic mis à jour", result.getDiagnostic());
        verify(consultationRepository, times(1)).save(any(Consultation.class));
    }

    // ============ TESTS POUR DELETE CONSULTATION ============

    @Test
    void deleteConsultation_Success() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        doNothing().when(consultationRepository).delete(consultation);

        // Act
        consultationService.deleteConsultation(1L);

        // Assert
        verify(consultationRepository, times(1)).delete(consultation);
    }

    // ============ TESTS POUR SEARCH CONSULTATIONS ============

    @Test
    void searchConsultations_ByPatientAndDateRange_Success() {
        // Arrange
        String dateDebut = "2024-01-01";
        String dateFin = "2024-01-31";
        List<Consultation> consultations = List.of(consultation);

        when(consultationRepository.findByIdPatientAndDateConsultationBetween(
                eq(1L), any(Date.class), any(Date.class)))
                .thenReturn(consultations);
        when(consultationMapper.toDTO(consultation)).thenReturn(consultationResponseDTO);

        // Act
        List<ConsultationResponseDTO> results = consultationService
                .searchConsultations(1L, dateDebut, dateFin);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }


    // ============ TESTS POUR EXAMENS CLINIQUES ============

    @Test
    void addExamenClinique_Success() {
        // Arrange
        ExamenCliniqueCreateDTO dto = ExamenCliniqueCreateDTO.builder()
                .typeExamen(TypeExamenClinique.TENSION)
                .valeur("12/8")
                .unite("mmHg")
                .build();

        ExamenClinique examen = ExamenClinique.builder()
                .idExamen(1L)
                .typeExamen(TypeExamenClinique.TENSION)
                .valeur("12/8")
                .unite("mmHg")
                .build();

        ExamenCliniqueDTO examenDTO = ExamenCliniqueDTO.builder()
                .idExamen(1L)
                .typeExamen(TypeExamenClinique.TENSION) // Utilisez .name()
                .valeur("12/8")
                .unite("mmHg")
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(examenCliniqueMapper.toEntity(dto)).thenReturn(examen);
        when(examenCliniqueRepository.save(examen)).thenReturn(examen);
        when(examenCliniqueMapper.toDTO(examen)).thenReturn(examenDTO);

        // Act
        ExamenCliniqueDTO result = consultationService.addExamenClinique(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(TypeExamenClinique.TENSION, result.getTypeExamen()); // Comparez avec String
        assertEquals("12/8", result.getValeur());
    }

    @Test
    void getExamensCliniques_Success() {
        // Arrange
        ExamenClinique examen = ExamenClinique.builder()
                .idExamen(1L)
                .typeExamen(TypeExamenClinique.TENSION)
                .valeur("12/8")
                .unite("mmHg")
                .build();

        consultation.setExamensCliniques(List.of(examen));

        ExamenCliniqueDTO examenDTO = ExamenCliniqueDTO.builder()
                .idExamen(1L)
                .typeExamen(TypeExamenClinique.TENSION) // String directement
                .valeur("12/8")
                .unite("mmHg")
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(examenCliniqueMapper.toDTO(examen)).thenReturn(examenDTO);

        // Act
        List<ExamenCliniqueDTO> results = consultationService.getExamensCliniques(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TypeExamenClinique.TENSION, results.get(0).getTypeExamen());
    }




    // ============ TESTS SIMPLIFIÉS POUR LES AUTRES MÉTHODES ============

    @Test
    void createOrdonnanceMedicament_Success() {
        // Arrange
        LigneOrdonnanceMedicamentCreateDTO ligneDTO = LigneOrdonnanceMedicamentCreateDTO.builder()
                .nomMedicament("Paracétamol")
                .posologie("1 comprimé 3 fois par jour")
                .duree("5 jours")
                .build();

        OrdonnanceMedicamentCreateDTO dto = OrdonnanceMedicamentCreateDTO.builder()
                .lignes(List.of(ligneDTO))
                .build();

        OrdonnanceMedicament ordonnance = OrdonnanceMedicament.builder()
                .idOrdonnance(1L)
                .build();

        OrdonnanceMedicamentDTO ordonnanceDTO = OrdonnanceMedicamentDTO.builder()
                .idOrdonnance(1L)
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(ordonnanceMedicamentMapper.toEntity(dto)).thenReturn(ordonnance);
        when(ordonnanceMedicamentRepository.save(ordonnance)).thenReturn(ordonnance);
        when(ordonnanceMedicamentMapper.toDTO(ordonnance)).thenReturn(ordonnanceDTO);

        // Act
        OrdonnanceMedicamentDTO result = consultationService.createOrdonnanceMedicament(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdOrdonnance());
    }

    @Test
    void createOrdonnanceExamen_Success() {
        // Arrange
        LigneOrdonnanceExamenCreateDTO ligneDTO = LigneOrdonnanceExamenCreateDTO.builder()
                .typeExamen(TypeExamenSupplementaire.BLOOD_TEST)
                .description("NFS complète")
                .build();

        OrdonnanceExamenCreateDTO dto = OrdonnanceExamenCreateDTO.builder()
                .lignes(List.of(ligneDTO))
                .build();

        OrdonnanceExamen ordonnance = OrdonnanceExamen.builder()
                .idOrdonnance(1L)
                .build();

        OrdonnanceExamenDTO ordonnanceDTO = OrdonnanceExamenDTO.builder()
                .idOrdonnance(1L)
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(ordonnanceExamenMapper.toEntity(dto)).thenReturn(ordonnance);
        when(ordonnanceExamenRepository.save(ordonnance)).thenReturn(ordonnance);
        when(ordonnanceExamenMapper.toDTO(ordonnance)).thenReturn(ordonnanceDTO);

        // Act
        OrdonnanceExamenDTO result = consultationService.createOrdonnanceExamen(1L, dto);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdOrdonnance());
    }

    @Test
    void getFactures_Success() {
        // Arrange
        Facture facture = Facture.builder()
                .idFacture(1L)
                .montantTotal(300.0)
                .statut("EN_ATTENTE")
                .build();

        consultation.setFactures(List.of(facture));

        FactureDTO factureDTO = FactureDTO.builder()
                .idFacture(1L)
                .montantTotal(300.0)
                .statut("EN_ATTENTE")
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(factureMapper.toDTO(facture)).thenReturn(factureDTO);

        // Act
        List<FactureDTO> results = consultationService.getFactures(1L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getIdFacture());
    }

    @Test
    void updateFactureStatut_Success() {
        // Arrange
        Facture facture = Facture.builder()
                .idFacture(1L)
                .statut("EN_ATTENTE")
                .build();

        Facture updatedFacture = Facture.builder()
                .idFacture(1L)
                .statut("PAYEE")
                .build();

        FactureDTO factureDTO = FactureDTO.builder()
                .idFacture(1L)
                .statut("PAYEE")
                .build();

        when(factureRepository.findById(1L)).thenReturn(Optional.of(facture));
        when(factureRepository.save(facture)).thenReturn(updatedFacture);
        when(factureMapper.toDTO(updatedFacture)).thenReturn(factureDTO);

        // Act
        FactureDTO result = consultationService.updateFactureStatut(1L, "PAYEE");

        // Assert
        assertNotNull(result);
        assertEquals("PAYEE", result.getStatut());
    }

    @Test
    void getAllFacturesByCabinet_Success() {
        // Arrange
        Facture facture = Facture.builder()
                .idFacture(1L)
                .cabinetId(3L)
                .montantTotal(300.0)
                .build();

        FactureDTO factureDTO = FactureDTO.builder()
                .idFacture(1L)
                .montantTotal(300.0)
                .build();

        when(factureRepository.findByCabinetIdOrderByDateFactureDesc(3L))
                .thenReturn(List.of(facture));
        when(factureMapper.toDTO(facture)).thenReturn(factureDTO);

        // Act
        List<FactureDTO> results = consultationService.getAllFacturesByCabinet(3L);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
    }

}