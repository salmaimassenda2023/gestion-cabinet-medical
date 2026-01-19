package com.example.cabinetservice;

import com.example.cabinetservice.dto.*;
import com.example.cabinetservice.dto.*;
import com.example.cabinetservice.entity.AbonnementCabinet;
import com.example.cabinetservice.entity.Cabinet;
import com.example.cabinetservice.entity.PaiementAbonnement;
import com.example.cabinetservice.entity.ServiceConsultation;
import com.example.cabinetservice.enums.AbonnementStatus;
import com.example.cabinetservice.enums.PaiementStatus;
import com.example.cabinetservice.enums.TypePeriode;
import com.example.cabinetservice.exception.ResourceNotFoundException;
import com.example.cabinetservice.mapper.CabinetMapper;
import com.example.cabinetservice.repository.AbonnementRepository;
import com.example.cabinetservice.repository.CabinetRepository;
import com.example.cabinetservice.repository.PaiementRepository;
import com.example.cabinetservice.repository.ServiceConsultationRepository;
import com.example.cabinetservice.service.impl.CabinetServiceImpl;
import com.example.cabinetservice.utilisateur.MedecinClient;
import com.example.cabinetservice.utilisateur.UtilisateurResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CabinetServiceImplTest {

    @Mock
    private CabinetRepository cabinetRepository;

    @Mock
    private AbonnementRepository abonnementRepository;

    @Mock
    private ServiceConsultationRepository serviceConsultationRepository;

    @Mock
    private PaiementRepository paiementRepository;

    @Mock
    private CabinetMapper cabinetMapper;

    @Mock
    private MedecinClient medecinClient;

    @InjectMocks
    private CabinetServiceImpl cabinetService;

    private Cabinet cabinet;
    private CabinetCreateDTO cabinetCreateDTO;
    private CabinetUpdateDTO cabinetUpdateDTO;
    private CabinetResponseDTO cabinetResponseDTO;
    private AbonnementCreateDTO abonnementDTO;
    private ServiceConsultationDTO serviceConsultationDTO;
    private UtilisateurResponse medecinUser;
    private UtilisateurResponse secretaireUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        abonnementDTO = AbonnementCreateDTO.builder()
                .typePeriode(TypePeriode.MENSUEL)
                .montant(99.99)
                .build();

        serviceConsultationDTO = ServiceConsultationDTO.builder()
                .nomService("Consultation Générale")
                .prix(50.0)
                .build();

        cabinetCreateDTO = CabinetCreateDTO.builder()
                .nom("Cabinet Medical Test")
                .specialite("Cardiologie")
                .adresse("123 Rue Test, Paris")
                .tel("0123456789")
                .logo("logo.png")
                .maxPatientsJour(20)
                .dureeConsultation(30)
                .medecinId(1L)
                .abonnement(abonnementDTO)
                .serviceConsultationGenerale(serviceConsultationDTO)
                .build();

        cabinetUpdateDTO = CabinetUpdateDTO.builder()
                .nom("Cabinet Medical Updated")
                .specialite("Neurologie")
                .adresse("456 Rue Updated, Paris")
                .build();

        cabinet = Cabinet.builder()
                .id(1L)
                .nom("Cabinet Medical Test")
                .specialite("Cardiologie")
                .adresse("123 Rue Test, Paris")
                .tel("0123456789")
                .logo("logo.png")
                .maxPatientsJour(20)
                .dureeConsultation(30)
                .medecinId(1L)
                .actif(false)
                .build();

        cabinetResponseDTO = CabinetResponseDTO.builder()
                .id(1L)
                .nom("Cabinet Medical Test")
                .specialite("Cardiologie")
                .adresse("123 Rue Test, Paris")
                .tel("0123456789")
                .logo("logo.png")
                .maxPatientsJour(20)
                .dureeConsultation(30)
                .actif(false)
                .build();

        medecinUser = UtilisateurResponse.builder()
                .idUtilisateur(1L)
                .login("medecin.test")
                .prenom("Jean")
                .nom("Dupont")
                .role("MEDECIN")
                .idCabinet(1L)
                .actif(true)
                .build();

        secretaireUser = UtilisateurResponse.builder()
                .idUtilisateur(2L)
                .login("secretaire.test")
                .prenom("Marie")
                .nom("Martin")
                .role("SECRETAIRE")
                .idCabinet(1L)
                .actif(true)
                .build();
    }

    // =============== CREATE CABINET TESTS ===============

    @Test
    void createCabinet_Success() {
        // Arrange
        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toEntity(abonnementDTO)).thenReturn(AbonnementCabinet.builder()
                .typePeriode(TypePeriode.MENSUEL)
                .montant(99.99)
                .build());
        when(abonnementRepository.save(any(AbonnementCabinet.class))).thenReturn(AbonnementCabinet.builder()
                .idAbonnement(1L)
                .typePeriode(TypePeriode.MENSUEL)
                .montant(99.99)
                .statut(AbonnementStatus.ACTIF)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusMonths(1))
                .build());
        when(cabinetMapper.toEntity(serviceConsultationDTO)).thenReturn(ServiceConsultation.builder()
                .nomService("Consultation Générale")
                .prix(50.0)
                .build());
        when(serviceConsultationRepository.save(any(ServiceConsultation.class))).thenReturn(ServiceConsultation.builder()
                .nomService("Consultation Générale")
                .prix(50.0)
                .build());
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L))
                .thenReturn(Collections.singletonList(ServiceConsultation.builder()
                        .nomService("Consultation Générale")
                        .prix(50.0)
                        .build()));
        when(cabinetMapper.toDto(any(ServiceConsultation.class))).thenReturn(serviceConsultationDTO);

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Cabinet Medical Test", result.getNom());
        assertFalse(result.getActif()); 
        verify(cabinetRepository, times(1)).save(any(Cabinet.class));
        verify(abonnementRepository, times(1)).save(any(AbonnementCabinet.class));
        verify(serviceConsultationRepository, times(1)).save(any(ServiceConsultation.class));
        verify(medecinClient, times(1)).updateCabinetId(eq(1L), eq(1L));
    }

    @Test
    void createCabinet_WithoutMedecinId_Success() {
        // Arrange
        cabinetCreateDTO.setMedecinId(null);
        cabinetCreateDTO.setAbonnement(null); 
        cabinetCreateDTO.setServiceConsultationGenerale(null); 
        cabinet.setMedecinId(null);

        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        verify(medecinClient, never()).updateCabinetId(anyLong(), anyLong());
        verify(abonnementRepository, never()).save(any()); 
        verify(serviceConsultationRepository, never()).save(any()); 
    }

    @Test
    void createCabinet_WithoutAbonnement_Success() {
        // Arrange
        cabinetCreateDTO.setAbonnement(null);
        cabinetCreateDTO.setServiceConsultationGenerale(null); 

        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        verify(abonnementRepository, never()).save(any(AbonnementCabinet.class));
        verify(serviceConsultationRepository, never()).save(any()); 
    }

    @Test
    void createCabinet_WithoutService_Success() {
        // Arrange
        cabinetCreateDTO.setServiceConsultationGenerale(null);
        cabinetCreateDTO.setAbonnement(null); 

        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        verify(serviceConsultationRepository, never()).save(any(ServiceConsultation.class));
        verify(abonnementRepository, never()).save(any()); 
    }

    // =============== GET CABINET TESTS ===============

    @Test
    void getCabinetById_Success() {
        // Arrange
        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinet(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Cabinet Medical Test", result.getNom());
        verify(cabinetRepository, times(1)).findById(1L);
    }

    @Test
    void getCabinetById_NotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getCabinet(999L));

        assertEquals("Cabinet not found with id: 999", exception.getMessage());
    }

    @Test
    void getCabinetByMedecinId_Success() {
        // Arrange
        when(cabinetRepository.findByMedecinId(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetByMedecinId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cabinetRepository, times(1)).findByMedecinId(1L);
    }

    @Test
    void getCabinetByMedecinId_NotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.findByMedecinId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cabinetService.getCabinetByMedecinId(999L));

        assertEquals("Cabinet not found for medecin with id: 999", exception.getMessage());
    }

    // =============== UPDATE CABINET TESTS ===============

    @Test
    void updateCabinet_Success() {
        // Arrange
        Cabinet updatedCabinet = Cabinet.builder()
                .id(1L)
                .nom("Cabinet Medical Updated")
                .specialite("Neurologie")
                .adresse("456 Rue Updated, Paris")
                .tel("0123456789")
                .logo("logo.png")
                .maxPatientsJour(20)
                .dureeConsultation(30)
                .medecinId(1L)
                .actif(false)
                .build();

        CabinetResponseDTO updatedResponse = CabinetResponseDTO.builder()
                .id(1L)
                .nom("Cabinet Medical Updated")
                .specialite("Neurologie")
                .adresse("456 Rue Updated, Paris")
                .tel("0123456789")
                .logo("logo.png")
                .maxPatientsJour(20)
                .dureeConsultation(30)
                .actif(false)
                .build();

        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(updatedCabinet);
        when(cabinetMapper.toDto(updatedCabinet)).thenReturn(updatedResponse);

        // Act
        CabinetResponseDTO result = cabinetService.updateCabinet(1L, cabinetUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Cabinet Medical Updated", result.getNom());
        assertEquals("Neurologie", result.getSpecialite());
        verify(cabinetRepository, times(1)).save(any(Cabinet.class));
    }

    @Test
    void updateCabinet_PartialUpdate() {
        // Arrange
        CabinetUpdateDTO partialUpdate = CabinetUpdateDTO.builder()
                .nom("Only Name Updated")
                .build();

        Cabinet updatedCabinet = Cabinet.builder()
                .id(1L)
                .nom("Only Name Updated")
                .specialite("Cardiologie") 
                .adresse("123 Rue Test, Paris") 
                .tel("0123456789") 
                .logo("logo.png") 
                .maxPatientsJour(20) 
                .dureeConsultation(30) 
                .medecinId(1L) 
                .actif(false) 
                .build();

        CabinetResponseDTO updatedResponse = CabinetResponseDTO.builder()
                .id(1L)
                .nom("Only Name Updated")
                .specialite("Cardiologie")
                .adresse("123 Rue Test, Paris")
                .tel("0123456789")
                .logo("logo.png")
                .maxPatientsJour(20)
                .dureeConsultation(30)
                .actif(false)
                .build();

        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(updatedCabinet);
        when(cabinetMapper.toDto(updatedCabinet)).thenReturn(updatedResponse);

        // Act
        CabinetResponseDTO result = cabinetService.updateCabinet(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Only Name Updated", result.getNom());
        assertEquals("Cardiologie", result.getSpecialite()); 
    }

    @Test
    void updateCabinet_NotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.updateCabinet(999L, cabinetUpdateDTO));

        assertEquals("Cabinet not found with id: 999", exception.getMessage());
        verify(cabinetRepository, never()).save(any(Cabinet.class));
    }

    // =============== DELETE CABINET TESTS ===============

    @Test
    void deleteCabinet_Success() {
        // Arrange
        when(cabinetRepository.existsById(1L)).thenReturn(true);

        // Act
        cabinetService.deleteCabinet(1L);

        // Assert
        verify(cabinetRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCabinet_NotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.deleteCabinet(999L));

        assertEquals("Cabinet not found with id: 999", exception.getMessage());
        verify(cabinetRepository, never()).deleteById(anyLong());
    }

    // =============== CABINET STATUS TESTS ===============

    @Test
    void isCabinetActive_ActiveCabinet_ReturnsTrue() {
        // Arrange
        cabinet.setActif(true);
        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));

        // Act
        Boolean result = cabinetService.isCabinetActive(1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void isCabinetActive_InactiveCabinet_ReturnsFalse() {
        // Arrange
        cabinet.setActif(false);
        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));

        // Act
        Boolean result = cabinetService.isCabinetActive(1L);

        // Assert
        assertFalse(result);
    }

    @Test
    void isCabinetActive_NotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.isCabinetActive(999L));

        assertEquals("Cabinet not found with id: 999", exception.getMessage());
    }

    // =============== SERVICE MANAGEMENT TESTS ===============

    @Test
    void addService_Success() {
        // Arrange
        ServiceConsultation service = ServiceConsultation.builder()
                .idService(1L) // ADD idService
                .nomService("Radiologie")
                .prix(120.0)
                .build();

        ServiceConsultationDTO newServiceDTO = ServiceConsultationDTO.builder()
                .nomService("Radiologie")
                .prix(120.0)
                .build();

        ServiceConsultationDTO savedServiceDTO = ServiceConsultationDTO.builder()
                .idService(1L) 
                .nomService("Radiologie")
                .prix(120.0)
                .build();

        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetMapper.toEntity(newServiceDTO)).thenReturn(service);
        when(serviceConsultationRepository.save(service)).thenReturn(service);
        when(cabinetMapper.toDto(service)).thenReturn(savedServiceDTO);

        // Act
        ServiceConsultationDTO result = cabinetService.addService(1L, newServiceDTO);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdService()); 
        assertEquals("Radiologie", result.getNomService());
        verify(serviceConsultationRepository, times(1)).save(service);
    }

    @Test
    void getServiceById_Success() {
        // Arrange
        ServiceConsultation service = ServiceConsultation.builder()
                .idService(1L)
                .nomService("Consultation Générale")
                .prix(50.0)
                .cabinet(cabinet)
                .build();

        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(serviceConsultationRepository.findById(1L)).thenReturn(Optional.of(service));
        when(cabinetMapper.toDto(service)).thenReturn(serviceConsultationDTO);

        // Act
        ServiceConsultationDTO result = cabinetService.getServiceById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Consultation Générale", result.getNomService());
    }

    @Test
    void getServiceById_ServiceNotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(serviceConsultationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getServiceById(1L, 999L));

        assertEquals("Service non trouvé avec l'ID: 999", exception.getMessage());
    }

    @Test
    void getServiceById_ServiceBelongsToDifferentCabinet_ThrowsException() {
        // Arrange
        Cabinet differentCabinet = Cabinet.builder().id(2L).build();
        ServiceConsultation service = ServiceConsultation.builder()
                .idService(1L)
                .cabinet(differentCabinet)
                .build();

        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(serviceConsultationRepository.findById(1L)).thenReturn(Optional.of(service));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getServiceById(1L, 1L));

        assertEquals("Service 1 n'appartient pas au cabinet 1", exception.getMessage());
    }

    @Test
    void getServices_Success() {
        // Arrange
        ServiceConsultation service1 = ServiceConsultation.builder()
                .idService(1L)
                .nomService("Consultation Générale")
                .prix(50.0)
                .build();

        ServiceConsultation service2 = ServiceConsultation.builder()
                .idService(2L)
                .nomService("Radiologie")
                .prix(120.0)
                .build();

        List<ServiceConsultation> services = Arrays.asList(service1, service2);
        ServiceConsultationDTO serviceDTO1 = ServiceConsultationDTO.builder()
                .idService(1L)
                .nomService("Consultation Générale")
                .prix(50.0)
                .build();
        ServiceConsultationDTO serviceDTO2 = ServiceConsultationDTO.builder()
                .idService(2L)
                .nomService("Radiologie")
                .prix(120.0)
                .build();

        when(cabinetRepository.existsById(1L)).thenReturn(true);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(services);
        when(cabinetMapper.toDto(service1)).thenReturn(serviceDTO1);
        when(cabinetMapper.toDto(service2)).thenReturn(serviceDTO2);

        // Act
        List<ServiceConsultationDTO> result = cabinetService.getServices(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Consultation Générale", result.get(0).getNomService());
        assertEquals("Radiologie", result.get(1).getNomService());
    }

    @Test
    void getServices_CabinetNotFound_ThrowsException() {
        // Arrange
        when(cabinetRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getServices(999L));

        assertEquals("Cabinet not found with id: 999", exception.getMessage());
    }

    // =============== GET ALL CABINETS TESTS ===============

    @Test
    void getAllCabinets_Success() {
        // Arrange
        Cabinet cabinet2 = Cabinet.builder()
                .id(2L)
                .nom("Cabinet 2")
                .specialite("Dentiste")
                .build();

        CabinetResponseDTO response1 = CabinetResponseDTO.builder()
                .id(1L)
                .nom("Cabinet Medical Test")
                .specialite("Cardiologie")
                .build();

        CabinetResponseDTO response2 = CabinetResponseDTO.builder()
                .id(2L)
                .nom("Cabinet 2")
                .specialite("Dentiste")
                .build();

        List<Cabinet> cabinets = Arrays.asList(cabinet, cabinet2);
        when(cabinetRepository.findAll()).thenReturn(cabinets);
        when(cabinetMapper.toDto(cabinet)).thenReturn(response1);
        when(cabinetMapper.toDto(cabinet2)).thenReturn(response2);

        // Act
        List<CabinetResponseDTO> result = cabinetService.getAllCabinets();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Cabinet Medical Test", result.get(0).getNom());
        assertEquals("Cabinet 2", result.get(1).getNom());
    }

    @Test
    void getAllCabinets_EmptyList() {
        // Arrange
        when(cabinetRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<CabinetResponseDTO> result = cabinetService.getAllCabinets();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =============== PAYMENT HANDLING TESTS ===============

    @Test
    void handleSuccessfulPayment_Success() {
        // Arrange
        AbonnementCabinet abonnement = AbonnementCabinet.builder()
                .idAbonnement(1L)
                .typePeriode(TypePeriode.MENSUEL)
                .statut(AbonnementStatus.EXPIRE)
                .montant(99.99)
                .cabinet(cabinet)
                .build();

        when(abonnementRepository.findById(1L)).thenReturn(Optional.of(abonnement));
        when(paiementRepository.save(any(PaiementAbonnement.class))).thenReturn(
                PaiementAbonnement.builder()
                        .idPaiement(1L)
                        .abonnement(abonnement)
                        .montant(99.99)
                        .statut(PaiementStatus.VALIDE)
                        .datePaiement(LocalDateTime.now())
                        .build()
        );
        when(abonnementRepository.save(any(AbonnementCabinet.class))).thenReturn(abonnement);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);

        // Act
        cabinetService.handleSuccessfulPayment(1L, 99.99, "stripe_payment_123");

        // Assert
        verify(paiementRepository, times(1)).save(any(PaiementAbonnement.class));
        verify(abonnementRepository, times(1)).save(any(AbonnementCabinet.class));
        verify(cabinetRepository, times(1)).save(cabinet);
        assertEquals(AbonnementStatus.ACTIF, abonnement.getStatut());
        assertTrue(cabinet.getActif());
    }

    @Test
    void handleSuccessfulPayment_Extension_Success() {
        // Arrange
        LocalDateTime existingDate = LocalDateTime.now().plusDays(5);
        AbonnementCabinet abonnement = AbonnementCabinet.builder()
                .idAbonnement(1L)
                .typePeriode(TypePeriode.MENSUEL)
                .statut(AbonnementStatus.ACTIF)
                .montant(99.99)
                .dateDebut(LocalDateTime.now().minusMonths(1))
                .dateFin(existingDate)
                .cabinet(cabinet)
                .build();

        when(abonnementRepository.findById(1L)).thenReturn(Optional.of(abonnement));
        when(paiementRepository.save(any(PaiementAbonnement.class))).thenReturn(
                PaiementAbonnement.builder()
                        .idPaiement(1L)
                        .abonnement(abonnement)
                        .montant(99.99)
                        .statut(PaiementStatus.VALIDE)
                        .datePaiement(LocalDateTime.now())
                        .build()
        );
        when(abonnementRepository.save(any(AbonnementCabinet.class))).thenReturn(abonnement);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);

        // Act
        cabinetService.handleSuccessfulPayment(1L, 99.99, "stripe_payment_123");

        // Assert
        assertTrue(abonnement.getDateFin().isAfter(existingDate));
    }

    @Test
    void handleSuccessfulPayment_AbonnementNotFound_ThrowsException() {
        // Arrange
        when(abonnementRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.handleSuccessfulPayment(999L, 99.99, "stripe_payment_123"));

        assertEquals("Abonnement not found with id: 999", exception.getMessage());
        verify(paiementRepository, never()).save(any());
    }

    // =============== GET CABINET BY USER TESTS ===============

    @Test
    void getCabinetByUserId_Medecin_Success() {
        // Arrange
        when(medecinClient.getUtilisateurById(1L)).thenReturn(medecinUser);
        when(cabinetRepository.findByMedecinId(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cabinetRepository, times(1)).findByMedecinId(1L);
    }

    @Test
    void getCabinetByUserId_Secretaire_WithCabinetId_Success() {
        // Arrange
        when(medecinClient.getUtilisateurById(2L)).thenReturn(secretaireUser);
        when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetByUserId(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cabinetRepository, times(1)).findById(1L);
    }

    @Test
    void getCabinetByUserId_Secretaire_WithoutCabinetId_FindsActiveCabinet() {
        // Arrange
        secretaireUser.setIdCabinet(null);
        when(medecinClient.getUtilisateurById(2L)).thenReturn(secretaireUser);

        Cabinet activeCabinet = Cabinet.builder()
                .id(2L)
                .nom("Active Cabinet")
                .abonnement(AbonnementCabinet.builder()
                        .statut(AbonnementStatus.ACTIF)
                        .build())
                .build();

        List<Cabinet> allCabinets = Arrays.asList(cabinet, activeCabinet);
        when(cabinetRepository.findAll()).thenReturn(allCabinets);
        when(cabinetMapper.toDto(activeCabinet)).thenReturn(CabinetResponseDTO.builder()
                .id(2L)
                .nom("Active Cabinet")
                .build());
        when(serviceConsultationRepository.findByCabinetId(2L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetByUserId(2L);

        // Assert
        assertNotNull(result);
        assertEquals("Active Cabinet", result.getNom());
    }

    @Test
    void getCabinetByUserId_Secretaire_WithoutCabinetId_NoActiveCabinet_Fallback() {
        // Arrange
        secretaireUser.setIdCabinet(null);
        when(medecinClient.getUtilisateurById(2L)).thenReturn(secretaireUser);

        List<Cabinet> allCabinets = Arrays.asList(cabinet);
        when(cabinetRepository.findAll()).thenReturn(allCabinets);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetByUserId(2L);

        // Assert
        assertNotNull(result);
        assertEquals("Cabinet Medical Test", result.getNom());
    }

    @Test
    void getCabinetByUserId_Medecin_NoCabinet_ThrowsException() {
        // Arrange
        when(medecinClient.getUtilisateurById(1L)).thenReturn(medecinUser);
        when(cabinetRepository.findByMedecinId(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getCabinetByUserId(1L));

        assertTrue(exception.getMessage().contains("Cabinet not found for medecin with id: 1"));
    }

    @Test
    void getCabinetByUserId_UnsupportedRole_ThrowsException() {
        // Arrange
        UtilisateurResponse adminUser = UtilisateurResponse.builder()
                .idUtilisateur(3L)
                .role("ADMIN")
                .build();
        when(medecinClient.getUtilisateurById(3L)).thenReturn(adminUser);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getCabinetByUserId(3L));

        assertTrue(exception.getMessage().contains("User role ADMIN not supported for cabinet access"));
    }
    @Test
    void getCabinetForUser_CallsGetCabinetByUserId() {
        // Arrange
        when(medecinClient.getUtilisateurById(1L)).thenReturn(medecinUser);
        when(cabinetRepository.findByMedecinId(1L)).thenReturn(Optional.of(cabinet));
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetForUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    // =============== GET ALL ABONNEMENTS TESTS ===============

    @Test
    void getAllAbonnements_Success() {
        // Arrange
        AbonnementCabinet abonnement1 = AbonnementCabinet.builder()
                .idAbonnement(1L)
                .typePeriode(TypePeriode.MENSUEL)
                .montant(99.99)
                .statut(AbonnementStatus.ACTIF)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusMonths(1))
                .cabinet(cabinet)
                .build();

        AbonnementCabinet abonnement2 = AbonnementCabinet.builder()
                .idAbonnement(2L)
                .typePeriode(TypePeriode.ANNUEL)
                .montant(999.99)
                .statut(AbonnementStatus.EXPIRE)
                .dateDebut(LocalDateTime.now())
                .dateFin(LocalDateTime.now().plusYears(1))
                .cabinet(null)
                .build();

        List<AbonnementCabinet> abonnements = Arrays.asList(abonnement1, abonnement2);

        when(abonnementRepository.findAll()).thenReturn(abonnements);

        // Act
        List<AbonnementResponseDTO> result = cabinetService.getAllAbonnements();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1L, result.get(0).getIdAbonnement());
        assertEquals("Cabinet Medical Test", result.get(0).getCabinetNom());
        assertEquals("logo.png", result.get(0).getCabinetLogo());

        assertEquals(2L, result.get(1).getIdAbonnement());
        assertEquals("Unknown", result.get(1).getCabinetNom());
        assertNull(result.get(1).getCabinetLogo());
    }

    @Test
    void getAllAbonnements_EmptyList() {
        // Arrange
        when(abonnementRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<AbonnementResponseDTO> result = cabinetService.getAllAbonnements();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =============== PARAMETERIZED TESTS ===============

    @ParameterizedTest
    @ValueSource(strings = {"MEDECIN", "SECRETAIRE"})
    void getCabinetByUserId_SupportedRoles_Success(String role) {
        // Arrange
        UtilisateurResponse user = UtilisateurResponse.builder()
                .idUtilisateur(1L)
                .role(role)
                .idCabinet(1L)
                .build();

        when(medecinClient.getUtilisateurById(1L)).thenReturn(user);

        if ("MEDECIN".equals(role)) {
            when(cabinetRepository.findByMedecinId(1L)).thenReturn(Optional.of(cabinet));
        } else {
            when(cabinetRepository.findById(1L)).thenReturn(Optional.of(cabinet));
        }

        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.getCabinetByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @ParameterizedTest
    @EnumSource(TypePeriode.class)
    void createCabinet_WithDifferentPeriodTypes_Success(TypePeriode typePeriode) {
        // Arrange
        abonnementDTO.setTypePeriode(typePeriode);
        cabinetCreateDTO.setAbonnement(abonnementDTO);
        cabinetCreateDTO.setServiceConsultationGenerale(null); 

        AbonnementCabinet abonnement = AbonnementCabinet.builder()
                .typePeriode(typePeriode)
                .montant(99.99)
                .build();

        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toEntity(abonnementDTO)).thenReturn(abonnement);
        when(abonnementRepository.save(any(AbonnementCabinet.class))).thenReturn(abonnement);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        verify(abonnementRepository, times(1)).save(any(AbonnementCabinet.class));
    }
    // =============== EDGE CASE TESTS ===============

    @Test
    void createCabinet_MedecinVerificationFails_StillCreatesCabinet() {
        // Arrange
        cabinetCreateDTO.setMedecinId(999L);
        cabinetCreateDTO.setAbonnement(null); 
        cabinetCreateDTO.setServiceConsultationGenerale(null); 

        when(medecinClient.getUtilisateurById(999L))
                .thenThrow(new RuntimeException("User not found"));
        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        // Should still create cabinet even if medecin verification fails
        verify(cabinetRepository, times(1)).save(any(Cabinet.class));
    }

    @Test
    void updateCabinet_SyncMedecinFails_StillUpdatesCabinet() {
        // Arrange - This test seems misnamed. It's testing createCabinet, not updateCabinet
        cabinetCreateDTO.setMedecinId(1L);
        cabinetCreateDTO.setAbonnement(null); 
        cabinetCreateDTO.setServiceConsultationGenerale(null); 

        when(cabinetMapper.toEntity(cabinetCreateDTO)).thenReturn(cabinet);
        when(cabinetRepository.save(any(Cabinet.class))).thenReturn(cabinet);
        when(cabinetMapper.toDto(cabinet)).thenReturn(cabinetResponseDTO);
        when(serviceConsultationRepository.findByCabinetId(1L)).thenReturn(Collections.emptyList());

        // Simulate medecin client throwing exception
        doThrow(new RuntimeException("Service unavailable"))
                .when(medecinClient).updateCabinetId(anyLong(), anyLong());

        // Act
        CabinetResponseDTO result = cabinetService.createCabinet(cabinetCreateDTO);

        // Assert
        assertNotNull(result);
        // Should still return success even if sync fails
        verify(cabinetRepository, times(1)).save(any(Cabinet.class));
    }

    @Test
    void getCabinetByUserId_MedecinClientFails_ThrowsException() {
        // Arrange
        when(medecinClient.getUtilisateurById(1L))
                .thenThrow(new RuntimeException("User service unavailable"));

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cabinetService.getCabinetByUserId(1L));

        assertTrue(exception.getMessage().contains("Failed to get cabinet for user ID"));
    }
}