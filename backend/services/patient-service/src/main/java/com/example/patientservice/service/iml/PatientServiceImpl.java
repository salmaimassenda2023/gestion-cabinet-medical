package com.example.patientservice.service.iml;


import com.example.patientservice.dto.*;
import com.example.patientservice.entity.DocumentMedical;
import com.example.patientservice.entity.DossierMedical;
import com.example.patientservice.entity.Patient;
import com.example.patientservice.exception.ResourceNotFoundException;
import com.example.patientservice.mapper.PatientMapper;
import com.example.patientservice.repository.DocumentMedicalRepository;
import com.example.patientservice.repository.DossierMedicalRepository;
import com.example.patientservice.repository.PatientRepository;
import com.example.patientservice.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final DossierMedicalRepository dossierMedicalRepository;
    private final DocumentMedicalRepository documentMedicalRepository;
    private final PatientMapper patientMapper;

    private static final String UPLOAD_DIR = "uploads/documents/";

    @Override
    @Transactional
    public PatientResponseDTO createPatient(PatientCreateDTO dto) {
        log.info("Creating new patient with CIN: {}", dto.getCin());

        // Vérifier si le CIN existe déjà
        if (patientRepository.existsByCin(dto.getCin())) {
            throw new RuntimeException("Un patient avec ce CIN existe déjà");
        }

        // Créer le patient
        Patient patient = patientMapper.toEntity(dto);
        patient = patientRepository.save(patient);

        // Créer automatiquement le dossier médical vide
        DossierMedical dossierMedical = DossierMedical.builder()
                .patient(patient)
                .build();
        dossierMedicalRepository.save(dossierMedical);
        patient.setDossierMedical(dossierMedical);

        log.info("Patient créé avec succès: ID={}", patient.getId());
        return patientMapper.toDto(patient);
    }
    @Override
    @Transactional(readOnly = true)
    public PatientInfoDTO getPatientInfo(Long id) {
        log.info("Récupération des infos basiques du patient ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient introuvable avec l'ID: " + id
                ));

        PatientInfoDTO dto = PatientInfoDTO.builder()
                .id(patient.getId())
                .cin(patient.getCin())
                .nom(patient.getNom())
                .prenom(patient.getPrenom())
                .dateNaissance(patient.getDateNaissance())
                .sexe(patient.getSexe())
                .telephone(patient.getTelephone())
                .email(patient.getEmail())
                .adresse(patient.getAdresse())
                .typeMutuelle(patient.getTypeMutuelle())
                .numeroMutuelle(patient.getNumeroMutuelle())
                .idCabinet(patient.getIdCabinet())
                .build();

        log.info("Infos basiques récupérées: {} {}",
                patient.getPrenom(), patient.getNom());

        return dto;
    }

    @Override
    public PatientResponseDTO getPatient(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id));
        return patientMapper.toDto(patient);
    }

    @Override
    @Transactional
    public PatientResponseDTO updatePatient(Long id, PatientUpdateDTO dto) {
        log.info("Updating patient ID: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id));

        if (dto.getNom() != null) patient.setNom(dto.getNom());
        if (dto.getPrenom() != null) patient.setPrenom(dto.getPrenom());
        if (dto.getTelephone() != null) patient.setTelephone(dto.getTelephone());
        if (dto.getEmail() != null) patient.setEmail(dto.getEmail());
        if (dto.getAdresse() != null) patient.setAdresse(dto.getAdresse());
        if (dto.getTypeMutuelle() != null) patient.setTypeMutuelle(dto.getTypeMutuelle());
        if (dto.getNumeroMutuelle() != null) patient.setNumeroMutuelle(dto.getNumeroMutuelle());
        if (dto.getSexe() != null) patient.setSexe(dto.getSexe());
        if (dto.getDateNaissance() != null) patient.setDateNaissance(dto.getDateNaissance());
        if (dto.getCin() != null) patient.setCin(dto.getCin());

        Patient updatedPatient = patientRepository.save(patient);
        return patientMapper.toDto(updatedPatient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient non trouvé avec l'ID: " + id);
        }
        patientRepository.deleteById(id);
        log.info("Patient supprimé: ID={}", id);
    }

    @Override
    public List<PatientResponseDTO> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientResponseDTO> getPatientsByCabinet(Long idCabinet) {
        return patientRepository.findByIdCabinet(idCabinet).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientResponseDTO> searchPatients(String searchTerm) {
        return patientRepository.searchPatients(searchTerm).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PatientResponseDTO> searchPatientsByCabinet(Long idCabinet, String searchTerm) {
        return patientRepository.searchPatientsByCabinet(idCabinet, searchTerm).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientResponseDTO getPatientByCin(String cin) {
        Patient patient = patientRepository.findByCin(cin)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec le CIN: " + cin));
        return patientMapper.toDto(patient);
    }

    @Override
    public DossierMedicalDTO getDossierMedical(Long patientId) {
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé pour le patient: " + patientId));
        return patientMapper.toDto(dossier);
    }

    @Override
    @Transactional
    public DossierMedicalDTO updateDossierMedical(Long patientId, DossierMedicalDTO dto) {
        log.info("Updating dossier médical for patient: {}", patientId);

        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé"));

        if (dto.getAntecedentsMedicaux() != null) dossier.setAntecedentsMedicaux(dto.getAntecedentsMedicaux());
        if (dto.getAntecedentsChirurgicaux() != null) dossier.setAntecedentsChirurgicaux(dto.getAntecedentsChirurgicaux());
        if (dto.getAllergies() != null) dossier.setAllergies(dto.getAllergies());
        if (dto.getGroupeSanguin() != null) dossier.setGroupeSanguin(dto.getGroupeSanguin());
        if (dto.getRemarques() != null) dossier.setRemarques(dto.getRemarques());

        DossierMedical updated = dossierMedicalRepository.save(dossier);
        return patientMapper.toDto(updated);
    }

    @Override
    @Transactional
    public DocumentMedicalDTO uploadDocument(Long patientId, MultipartFile file, String type) {
        log.info("Uploading document for patient: {}", patientId);

        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé"));

        try {
            // Créer le dossier s'il n'existe pas
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Générer un nom unique pour le fichier
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            // Sauvegarder le fichier
            Files.copy(file.getInputStream(), filePath);

            // Créer l'entité DocumentMedical
            DocumentMedical document = DocumentMedical.builder()
                    .dossierMedical(dossier)
                    .type(type)
                    .nom(originalFilename)
                    .url(UPLOAD_DIR + filename)
                    .tailleOctets(file.getSize())
                    .build();

            document = documentMedicalRepository.save(document);
            log.info("Document uploadé: {}", filename);

            return patientMapper.toDto(document);

        } catch (IOException e) {
            log.error("Erreur lors de l'upload du document", e);
            throw new RuntimeException("Erreur lors de l'upload du document", e);
        }
    }

    @Override
    public List<DocumentMedicalDTO> getDocuments(Long patientId) {
        DossierMedical dossier = dossierMedicalRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Dossier médical non trouvé"));

        return documentMedicalRepository.findByDossierMedical_PatientId(dossier.getId()).stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        DocumentMedical document = documentMedicalRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé"));

        try {
            // Supprimer le fichier physique
            Path filePath = Paths.get(document.getUrl());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier", e);
        }

        documentMedicalRepository.delete(document);
        log.info("Document supprimé: ID={}", documentId);
    }
}