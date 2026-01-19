package com.example.patientservice;

import com.example.patientservice.dto.*;
import com.example.patientservice.entity.DocumentMedical;
import com.example.patientservice.entity.DossierMedical;
import com.example.patientservice.entity.Patient;
import com.example.patientservice.exception.ResourceNotFoundException;
import com.example.patientservice.mapper.PatientMapper;
import com.example.patientservice.repository.DocumentMedicalRepository;
import com.example.patientservice.repository.DossierMedicalRepository;
import com.example.patientservice.repository.PatientRepository;
import com.example.patientservice.service.iml.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DossierMedicalRepository dossierMedicalRepository;

    @Mock
    private DocumentMedicalRepository documentMedicalRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient patient;
    private PatientCreateDTO patientCreateDTO;
    private PatientUpdateDTO patientUpdateDTO;
    private PatientResponseDTO patientResponseDTO;
    private PatientInfoDTO patientInfoDTO;
    private DossierMedical dossierMedical;
    private DossierMedicalDTO dossierMedicalDTO;
    private DocumentMedical documentMedical;
    private DocumentMedicalDTO documentMedicalDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        patientCreateDTO = PatientCreateDTO.builder()
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        patientUpdateDTO = PatientUpdateDTO.builder()
                .nom("Dupont Updated")
                .prenom("Jean Updated")
                .telephone("0698765432")
                .email("jean.updated@example.com")
                .build();

        patient = Patient.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        patientResponseDTO = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        patientInfoDTO = PatientInfoDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        dossierMedical = DossierMedical.builder()
                .id(1L)
                .patient(patient)
                .antecedentsMedicaux("Hypertension")
                .antecedentsChirurgicaux("Appendicectomie 2010")
                .allergies("Pénicilline")
                .groupeSanguin("O+")
                .remarques("Patient régulier")
                .dateCreation(LocalDateTime.now())
                .build();

        dossierMedicalDTO = DossierMedicalDTO.builder()
                .idDossier(1L)
                .antecedentsMedicaux("Hypertension")
                .antecedentsChirurgicaux("Appendicectomie 2010")
                .allergies("Pénicilline")
                .groupeSanguin("O+")
                .remarques("Patient régulier")
                .build();

        documentMedical = DocumentMedical.builder()
                .idDocument(1L)
                .dossierMedical(dossierMedical)
                .type("RADIOLOGIE")
                .nom("scan-poumon.pdf")
                .url("uploads/documents/uuid-scan.pdf")
                .tailleOctets(2048000L)
                .build();

        documentMedicalDTO = DocumentMedicalDTO.builder()
                .idDocument(1L)
                .type("RADIOLOGIE")
                .nom("scan-poumon.pdf")
                .url("uploads/documents/uuid-scan.pdf")
                .tailleOctets(2048000L)
                .dateUpload(LocalDateTime.now())
                .build();
    }

    // =============== CREATE PATIENT TESTS ===============

    @Test
    void createPatient_Success() {
        // Arrange
        when(patientRepository.existsByCin("AB123456")).thenReturn(false);
        when(patientMapper.toEntity(patientCreateDTO)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(dossierMedicalRepository.save(any(DossierMedical.class))).thenReturn(dossierMedical);
        when(patientMapper.toDto(patient)).thenReturn(patientResponseDTO);

        // Act
        PatientResponseDTO result = patientService.createPatient(patientCreateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("AB123456", result.getCin());
        assertEquals("Dupont", result.getNom());
        assertEquals("Jean", result.getPrenom());
        verify(patientRepository, times(1)).existsByCin("AB123456");
        verify(patientRepository, times(1)).save(any(Patient.class));
        verify(dossierMedicalRepository, times(1)).save(any(DossierMedical.class));
    }

    @Test
    void createPatient_DuplicateCin_ThrowsException() {
        // Arrange
        when(patientRepository.existsByCin("AB123456")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> patientService.createPatient(patientCreateDTO));

        assertEquals("Un patient avec ce CIN existe déjà", exception.getMessage());
        verify(patientRepository, times(1)).existsByCin("AB123456");
        verify(patientRepository, never()).save(any(Patient.class));
        verify(dossierMedicalRepository, never()).save(any(DossierMedical.class));
    }

    // =============== GET PATIENT INFO TESTS ===============

    @Test
    void getPatientInfo_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // Act
        PatientInfoDTO result = patientService.getPatientInfo(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Dupont", result.getNom());
        assertEquals("Jean", result.getPrenom());
        assertEquals("AB123456", result.getCin());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void getPatientInfo_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientInfo(999L));

        assertEquals("Patient introuvable avec l'ID: 999", exception.getMessage());
        verify(patientRepository, times(1)).findById(999L);
    }

    // =============== GET PATIENT TESTS ===============

    @Test
    void getPatient_Success() {
        // Arrange
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientResponseDTO);

        // Act
        PatientResponseDTO result = patientService.getPatient(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Dupont", result.getNom());
        verify(patientRepository, times(1)).findById(1L);
        verify(patientMapper, times(1)).toDto(patient);
    }

    @Test
    void getPatient_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatient(999L));

        assertEquals("Patient non trouvé avec l'ID: 999", exception.getMessage());
        verify(patientRepository, times(1)).findById(999L);
    }

    // =============== UPDATE PATIENT TESTS ===============

    @Test
    void updatePatient_Success() {
        // Arrange
        Patient updatedPatient = Patient.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont Updated")
                .prenom("Jean Updated")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0698765432")
                .email("jean.updated@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        PatientResponseDTO updatedResponse = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont Updated")
                .prenom("Jean Updated")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0698765432")
                .email("jean.updated@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);
        when(patientMapper.toDto(updatedPatient)).thenReturn(updatedResponse);

        // Act
        PatientResponseDTO result = patientService.updatePatient(1L, patientUpdateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Dupont Updated", result.getNom());
        assertEquals("Jean Updated", result.getPrenom());
        assertEquals("0698765432", result.getTelephone());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void updatePatient_PartialUpdate() {
        // Arrange
        PatientUpdateDTO partialUpdate = PatientUpdateDTO.builder()
                .nom("Only Name Updated")
                .build();

        Patient updatedPatient = Patient.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Only Name Updated")
                .prenom("Jean")         
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678") 
                .email("jean.dupont@example.com") 
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        PatientResponseDTO updatedResponse = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Only Name Updated")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);
        when(patientMapper.toDto(updatedPatient)).thenReturn(updatedResponse);

        // Act
        PatientResponseDTO result = patientService.updatePatient(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Only Name Updated", result.getNom());
        assertEquals("Jean", result.getPrenom()); 
    }

    @Test
    void updatePatient_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.updatePatient(999L, patientUpdateDTO));

        assertEquals("Patient non trouvé avec l'ID: 999", exception.getMessage());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    // =============== DELETE PATIENT TESTS ===============

    @Test
    void deletePatient_Success() {
        // Arrange
        when(patientRepository.existsById(1L)).thenReturn(true);

        // Act
        patientService.deletePatient(1L);

        // Assert
        verify(patientRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePatient_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.deletePatient(999L));

        assertEquals("Patient non trouvé avec l'ID: 999", exception.getMessage());
        verify(patientRepository, never()).deleteById(anyLong());
    }

    // =============== GET ALL PATIENTS TESTS ===============

    @Test
    void getAllPatients_Success() {
        // Arrange
        Patient patient2 = Patient.builder()
                .id(2L)
                .cin("CD789012")
                .nom("Martin")
                .prenom("Marie")
                .build();

        PatientResponseDTO response1 = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .build();

        PatientResponseDTO response2 = PatientResponseDTO.builder()
                .id(2L)
                .cin("CD789012")
                .nom("Martin")
                .prenom("Marie")
                .build();

        List<Patient> patients = Arrays.asList(patient, patient2);
        when(patientRepository.findAll()).thenReturn(patients);
        when(patientMapper.toDto(patient)).thenReturn(response1);
        when(patientMapper.toDto(patient2)).thenReturn(response2);

        // Act
        List<PatientResponseDTO> result = patientService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dupont", result.get(0).getNom());
        assertEquals("Martin", result.get(1).getNom());
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void getAllPatients_EmptyList() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PatientResponseDTO> result = patientService.getAllPatients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =============== GET PATIENTS BY CABINET TESTS ===============

    @Test
    void getPatientsByCabinet_Success() {
        // Arrange
        Patient patient2 = Patient.builder()
                .id(2L)
                .cin("CD789012")
                .nom("Martin")
                .prenom("Marie")
                .idCabinet(1L)
                .build();

        PatientResponseDTO response1 = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .idCabinet(1L)
                .build();

        PatientResponseDTO response2 = PatientResponseDTO.builder()
                .id(2L)
                .cin("CD789012")
                .nom("Martin")
                .prenom("Marie")
                .idCabinet(1L)
                .build();

        List<Patient> patients = Arrays.asList(patient, patient2);
        when(patientRepository.findByIdCabinet(1L)).thenReturn(patients);
        when(patientMapper.toDto(patient)).thenReturn(response1);
        when(patientMapper.toDto(patient2)).thenReturn(response2);

        // Act
        List<PatientResponseDTO> result = patientService.getPatientsByCabinet(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(p -> p.getIdCabinet() == 1L));
        verify(patientRepository, times(1)).findByIdCabinet(1L);
    }

    @Test
    void getPatientsByCabinet_EmptyList() {
        // Arrange
        when(patientRepository.findByIdCabinet(999L)).thenReturn(Collections.emptyList());

        // Act
        List<PatientResponseDTO> result = patientService.getPatientsByCabinet(999L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =============== SEARCH PATIENTS TESTS ===============

    @Test
    void searchPatients_Success() {
        // Arrange
        String searchTerm = "Dupont";
        PatientResponseDTO response = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .build();

        when(patientRepository.searchPatients(searchTerm)).thenReturn(Collections.singletonList(patient));
        when(patientMapper.toDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDTO> result = patientService.searchPatients(searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dupont", result.get(0).getNom());
        verify(patientRepository, times(1)).searchPatients(searchTerm);
    }

    @Test
    void searchPatientsByCabinet_Success() {
        // Arrange
        String searchTerm = "Dupont";
        PatientResponseDTO response = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .idCabinet(1L)
                .build();

        when(patientRepository.searchPatientsByCabinet(1L, searchTerm))
                .thenReturn(Collections.singletonList(patient));
        when(patientMapper.toDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDTO> result = patientService.searchPatientsByCabinet(1L, searchTerm);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Dupont", result.get(0).getNom());
        verify(patientRepository, times(1)).searchPatientsByCabinet(1L, searchTerm);
    }

    // =============== GET PATIENT BY CIN TESTS ===============

    @Test
    void getPatientByCin_Success() {
        // Arrange
        when(patientRepository.findByCin("AB123456")).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientResponseDTO);

        // Act
        PatientResponseDTO result = patientService.getPatientByCin("AB123456");

        // Assert
        assertNotNull(result);
        assertEquals("AB123456", result.getCin());
        verify(patientRepository, times(1)).findByCin("AB123456");
    }

    @Test
    void getPatientByCin_NotFound_ThrowsException() {
        // Arrange
        when(patientRepository.findByCin("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientByCin("INVALID"));

        assertEquals("Patient non trouvé avec le CIN: INVALID", exception.getMessage());
        verify(patientRepository, times(1)).findByCin("INVALID");
    }

    // =============== DOSSIER MEDICAL TESTS ===============

    @Test
    void getDossierMedical_Success() {
        // Arrange
        when(dossierMedicalRepository.findByPatientId(1L)).thenReturn(Optional.of(dossierMedical));
        when(patientMapper.toDto(dossierMedical)).thenReturn(dossierMedicalDTO);

        // Act
        DossierMedicalDTO result = patientService.getDossierMedical(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Hypertension", result.getAntecedentsMedicaux());
        assertEquals("Pénicilline", result.getAllergies());
        verify(dossierMedicalRepository, times(1)).findByPatientId(1L);
    }

    @Test
    void getDossierMedical_NotFound_ThrowsException() {
        // Arrange
        when(dossierMedicalRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.getDossierMedical(999L));

        assertEquals("Dossier médical non trouvé pour le patient: 999", exception.getMessage());
        verify(dossierMedicalRepository, times(1)).findByPatientId(999L);
    }

    @Test
    void updateDossierMedical_Success() {
        // Arrange
        DossierMedical updatedDossier = DossierMedical.builder()
                .id(1L)
                .patient(patient)
                .antecedentsMedicaux("Hypertension, Diabète")
                .antecedentsChirurgicaux("Appendicectomie 2010")
                .allergies("Pénicilline, Aspirine")
                .groupeSanguin("O+")
                .remarques("Patient régulier - Suivi trimestriel")
                .build();

        DossierMedicalDTO updateDTO = DossierMedicalDTO.builder()
                .antecedentsMedicaux("Hypertension, Diabète")
                .allergies("Pénicilline, Aspirine")
                .remarques("Patient régulier - Suivi trimestriel")
                .build();

        DossierMedicalDTO updatedResponse = DossierMedicalDTO.builder()
                .idDossier(1L)
                .antecedentsMedicaux("Hypertension, Diabète")
                .antecedentsChirurgicaux("Appendicectomie 2010")
                .allergies("Pénicilline, Aspirine")
                .groupeSanguin("O+")
                .remarques("Patient régulier - Suivi trimestriel")
                .build();

        when(dossierMedicalRepository.findByPatientId(1L)).thenReturn(Optional.of(dossierMedical));
        when(dossierMedicalRepository.save(any(DossierMedical.class))).thenReturn(updatedDossier);
        when(patientMapper.toDto(updatedDossier)).thenReturn(updatedResponse);

        // Act
        DossierMedicalDTO result = patientService.updateDossierMedical(1L, updateDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Hypertension, Diabète", result.getAntecedentsMedicaux());
        assertEquals("Pénicilline, Aspirine", result.getAllergies());
        verify(dossierMedicalRepository, times(1)).save(any(DossierMedical.class));
    }

    @Test
    void updateDossierMedical_PartialUpdate() {
        // Arrange
        DossierMedicalDTO partialUpdate = DossierMedicalDTO.builder()
                .antecedentsMedicaux("Only this updated")
                .build();

        DossierMedical updatedDossier = DossierMedical.builder()
                .id(1L)
                .patient(patient)
                .antecedentsMedicaux("Only this updated")
                .antecedentsChirurgicaux("Appendicectomie 2010") // unchanged
                .allergies("Pénicilline") // unchanged
                .groupeSanguin("O+") // unchanged
                .remarques("Patient régulier") // unchanged
                .build();

        DossierMedicalDTO updatedResponse = DossierMedicalDTO.builder()
                .idDossier(1L)
                .antecedentsMedicaux("Only this updated")
                .antecedentsChirurgicaux("Appendicectomie 2010")
                .allergies("Pénicilline")
                .groupeSanguin("O+")
                .remarques("Patient régulier")
                .build();

        when(dossierMedicalRepository.findByPatientId(1L)).thenReturn(Optional.of(dossierMedical));
        when(dossierMedicalRepository.save(any(DossierMedical.class))).thenReturn(updatedDossier);
        when(patientMapper.toDto(updatedDossier)).thenReturn(updatedResponse);

        // Act
        DossierMedicalDTO result = patientService.updateDossierMedical(1L, partialUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Only this updated", result.getAntecedentsMedicaux());
        assertEquals("Pénicilline", result.getAllergies()); // unchanged
    }

    @Test
    void updateDossierMedical_NotFound_ThrowsException() {
        // Arrange
        when(dossierMedicalRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.updateDossierMedical(999L, dossierMedicalDTO));

        assertEquals("Dossier médical non trouvé", exception.getMessage());
        verify(dossierMedicalRepository, never()).save(any(DossierMedical.class));
    }

    // =============== DOCUMENT MEDICAL TESTS ===============

    @Test
    void uploadDocument_Success() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "scan.pdf",
                "scan.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(dossierMedicalRepository.findByPatientId(1L)).thenReturn(Optional.of(dossierMedical));
        when(documentMedicalRepository.save(any(DocumentMedical.class))).thenReturn(documentMedical);
        when(patientMapper.toDto(documentMedical)).thenReturn(documentMedicalDTO);

        // Act
        DocumentMedicalDTO result = patientService.uploadDocument(1L, file, "RADIOLOGIE");

        // Assert
        assertNotNull(result);
        assertEquals("RADIOLOGIE", result.getType());
        assertEquals("scan-poumon.pdf", result.getNom());
        verify(documentMedicalRepository, times(1)).save(any(DocumentMedical.class));
    }

    @Test
    void uploadDocument_DossierNotFound_ThrowsException() {
        // Arrange
        MultipartFile file = new MockMultipartFile(
                "scan.pdf",
                "scan.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(dossierMedicalRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.uploadDocument(999L, file, "RADIOLOGIE"));

        assertEquals("Dossier médical non trouvé", exception.getMessage());
        verify(documentMedicalRepository, never()).save(any(DocumentMedical.class));
    }



    @Test
    void getDocuments_Success() {
        // Arrange
        when(dossierMedicalRepository.findByPatientId(1L)).thenReturn(Optional.of(dossierMedical));
        when(documentMedicalRepository.findByDossierMedical_PatientId(1L))
                .thenReturn(Collections.singletonList(documentMedical));
        when(patientMapper.toDto(documentMedical)).thenReturn(documentMedicalDTO);

        // Act
        List<DocumentMedicalDTO> result = patientService.getDocuments(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("RADIOLOGIE", result.get(0).getType());
        verify(documentMedicalRepository, times(1)).findByDossierMedical_PatientId(1L);
    }

    @Test
    void getDocuments_DossierNotFound_ThrowsException() {
        // Arrange
        when(dossierMedicalRepository.findByPatientId(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.getDocuments(999L));

        assertEquals("Dossier médical non trouvé", exception.getMessage());
        verify(documentMedicalRepository, never()).findByDossierMedical_PatientId(anyLong());
    }

    @Test
    void deleteDocument_Success() throws IOException {
        // Arrange
        when(documentMedicalRepository.findById(1L)).thenReturn(Optional.of(documentMedical));

        // Mock the static Paths.get() and Files.deleteIfExists()
        Path mockPath = mock(Path.class);
        try (var mockedPaths = mockStatic(Paths.class);
             var mockedFiles = mockStatic(Files.class)) {

            mockedPaths.when(() -> Paths.get(documentMedical.getUrl())).thenReturn(mockPath);
            mockedFiles.when(() -> Files.deleteIfExists(mockPath)).thenReturn(true);

            // Act
            patientService.deleteDocument(1L);
        }

        // Assert
        verify(documentMedicalRepository, times(1)).delete(documentMedical);
    }

    @Test
    void deleteDocument_DocumentNotFound_ThrowsException() {
        // Arrange
        when(documentMedicalRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> patientService.deleteDocument(999L));

        assertEquals("Document non trouvé", exception.getMessage());
        verify(documentMedicalRepository, never()).delete(any(DocumentMedical.class));
    }

    @Test
    void deleteDocument_IOException_StillDeletesFromDatabase() throws IOException {
        // Arrange
        when(documentMedicalRepository.findById(1L)).thenReturn(Optional.of(documentMedical));

        // Mock the static methods
        Path mockPath = mock(Path.class);
        try (var mockedPaths = mockStatic(Paths.class);
             var mockedFiles = mockStatic(Files.class)) {

            mockedPaths.when(() -> Paths.get(documentMedical.getUrl())).thenReturn(mockPath);
            mockedFiles.when(() -> Files.deleteIfExists(mockPath)).thenThrow(new IOException("File delete error"));

            // Act
            patientService.deleteDocument(1L);
        }

        // Assert - Should still delete from database even if file delete fails
        verify(documentMedicalRepository, times(1)).delete(documentMedical);
    }

    // =============== EDGE CASE TESTS ===============

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "  "})
    void searchPatients_EmptySearchTerm_ReturnsResults(String searchTerm) {
        // Arrange
        PatientResponseDTO response = PatientResponseDTO.builder()
                .id(1L)
                .cin("AB123456")
                .nom("Dupont")
                .prenom("Jean")
                .build();

        when(patientRepository.searchPatients(searchTerm)).thenReturn(Collections.singletonList(patient));
        when(patientMapper.toDto(patient)).thenReturn(response);

        // Act
        List<PatientResponseDTO> result = patientService.searchPatients(searchTerm);

        // Assert
        assertNotNull(result);
        verify(patientRepository, times(1)).searchPatients(searchTerm);
    }

    @Test
    void updatePatient_UpdateCin_ShouldUpdate() {
        // Arrange
        PatientUpdateDTO updateWithCin = PatientUpdateDTO.builder()
                .cin("NEW123456")
                .build();

        Patient updatedPatient = Patient.builder()
                .id(1L)
                .cin("NEW123456")
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        PatientResponseDTO updatedResponse = PatientResponseDTO.builder()
                .id(1L)
                .cin("NEW123456")
                .nom("Dupont")
                .prenom("Jean")
                .dateNaissance(LocalDate.of(1980, 5, 15))
                .sexe("M")
                .telephone("0612345678")
                .email("jean.dupont@example.com")
                .adresse("123 Rue de Paris, 75001 Paris")
                .typeMutuelle("CNOPS")
                .numeroMutuelle("CNOPS123456")
                .idCabinet(1L)
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(updatedPatient);
        when(patientMapper.toDto(updatedPatient)).thenReturn(updatedResponse);

        // Act
        PatientResponseDTO result = patientService.updatePatient(1L, updateWithCin);

        // Assert
        assertNotNull(result);
        assertEquals("NEW123456", result.getCin());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void createPatient_NullValues_ShouldStillCreate() {
        // Arrange
        PatientCreateDTO minimalDTO = PatientCreateDTO.builder()
                .cin("MIN123456")
                .nom("Minimal")
                .prenom("Patient")
                .build();

        Patient minimalPatient = Patient.builder()
                .id(3L)
                .cin("MIN123456")
                .nom("Minimal")
                .prenom("Patient")
                .build();

        PatientResponseDTO minimalResponse = PatientResponseDTO.builder()
                .id(3L)
                .cin("MIN123456")
                .nom("Minimal")
                .prenom("Patient")
                .build();

        when(patientRepository.existsByCin("MIN123456")).thenReturn(false);
        when(patientMapper.toEntity(minimalDTO)).thenReturn(minimalPatient);
        when(patientRepository.save(any(Patient.class))).thenReturn(minimalPatient);
        when(dossierMedicalRepository.save(any(DossierMedical.class))).thenReturn(dossierMedical);
        when(patientMapper.toDto(minimalPatient)).thenReturn(minimalResponse);

        // Act
        PatientResponseDTO result = patientService.createPatient(minimalDTO);

        // Assert
        assertNotNull(result);
        assertEquals("MIN123456", result.getCin());
        assertEquals("Minimal", result.getNom());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void getPatientInfo_MinimalPatient_ReturnsInfo() {
        // Arrange
        Patient minimalPatient = Patient.builder()
                .id(3L)
                .cin("MIN123456")
                .nom("Minimal")
                .prenom("Patient")
                .build();

        when(patientRepository.findById(3L)).thenReturn(Optional.of(minimalPatient));

        // Act
        PatientInfoDTO result = patientService.getPatientInfo(3L);

        // Assert
        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("MIN123456", result.getCin());
        assertEquals("Minimal", result.getNom());
        assertEquals("Patient", result.getPrenom());
        assertNull(result.getTelephone()); // Should be null
        assertNull(result.getEmail()); // Should be null
        verify(patientRepository, times(1)).findById(3L);
    }
}