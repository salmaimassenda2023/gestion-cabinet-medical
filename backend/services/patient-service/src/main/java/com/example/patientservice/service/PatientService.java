package com.example.patientservice.service;


import com.example.patientservice.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PatientService {

    // CRUD Patient
    PatientResponseDTO createPatient(PatientCreateDTO dto);
    PatientResponseDTO getPatient(Long id);
    PatientResponseDTO updatePatient(Long id, PatientUpdateDTO dto);
    void deletePatient(Long id);

    // Recherche
    List<PatientResponseDTO> getAllPatients();
    List<PatientResponseDTO> getPatientsByCabinet(Long idCabinet);
    List<PatientResponseDTO> searchPatients(String searchTerm);
    List<PatientResponseDTO> searchPatientsByCabinet(Long idCabinet, String searchTerm);
    PatientResponseDTO getPatientByCin(String cin);

    // Dossier Médical
    DossierMedicalDTO getDossierMedical(Long patientId);
    DossierMedicalDTO updateDossierMedical(Long patientId, DossierMedicalDTO dto);

    // Documents
    DocumentMedicalDTO uploadDocument(Long patientId, MultipartFile file, String type);
    List<DocumentMedicalDTO> getDocuments(Long patientId);
    void deleteDocument(Long documentId);
}
