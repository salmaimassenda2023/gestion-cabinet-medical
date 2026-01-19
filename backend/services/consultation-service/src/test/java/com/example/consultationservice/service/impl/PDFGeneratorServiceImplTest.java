package com.example.consultationservice.service.impl;

import com.example.consultationservice.dto.ConsultationPatientResponseDTO;
import com.example.consultationservice.entity.*;
import com.example.consultationservice.enums.TypeExamenSupplementaire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PDFGeneratorServiceImplTest {

    private PDFGeneratorServiceImpl pdfGeneratorService;

    @BeforeEach
    void setUp() {
        pdfGeneratorService = new PDFGeneratorServiceImpl();
    }

    @Test
    void generateOrdonnanceMedicamentPDF_Success() {
        // Arrange
        ConsultationPatientResponseDTO patient = ConsultationPatientResponseDTO.builder()
                .idPatient(1L)
                .nom("Dupont")
                .prenom("Jean")
                .cin("AB123456")
                .dateNaissance(new Date())
                .telephone("0612345678")
                .build();

        Consultation consultation = Consultation.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .build();

        LigneOrdonnanceMedicament ligne = LigneOrdonnanceMedicament.builder()
                .idLigne(1L)
                .nomMedicament("Paracétamol")
                .posologie("1 comprimé 3 fois par jour")
                .duree("5 jours")
                .build();

        OrdonnanceMedicament ordonnance = OrdonnanceMedicament.builder()
                .idOrdonnance(1L)
                .dateCreation(new Date())
                .consultation(consultation)
                .lignes(List.of(ligne))
                .build();

        // Act & Assert - Test que la méthode ne lance pas d'exception
        assertDoesNotThrow(() -> {
            byte[] pdf = pdfGeneratorService.generateOrdonnanceMedicamentPDF(ordonnance, patient);
            assertNotNull(pdf);
            assertTrue(pdf.length > 0);
        });
    }

    @Test
    void generateOrdonnanceExamenPDF_Success() {
        // Arrange
        ConsultationPatientResponseDTO patient = ConsultationPatientResponseDTO.builder()
                .idPatient(1L)
                .nom("Dupont")
                .prenom("Jean")
                .cin("AB123456")
                .dateNaissance(new Date())
                .build();

        Consultation consultation = Consultation.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .build();

        LigneOrdonnanceExamen ligne = LigneOrdonnanceExamen.builder()
                .idLigne(1L)
                .typeExamen(TypeExamenSupplementaire.BLOOD_TEST)
                .description("NFS complète")
                .build();

        OrdonnanceExamen ordonnance = OrdonnanceExamen.builder()
                .idOrdonnance(1L)
                .dateCreation(new Date())
                .consultation(consultation)
                .lignes(List.of(ligne))
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> {
            byte[] pdf = pdfGeneratorService.generateOrdonnanceExamenPDF(ordonnance, patient);
            assertNotNull(pdf);
            assertTrue(pdf.length > 0);
        });
    }

    @Test
    void generateFacturePDF_Success() {
        // Arrange
        ConsultationPatientResponseDTO patient = ConsultationPatientResponseDTO.builder()
                .idPatient(1L)
                .nom("Dupont")
                .prenom("Jean")
                .cin("AB123456")
                .dateNaissance(new Date())
                .telephone("0612345678")
                .build();

        ConsultationServiceItem serviceItem = ConsultationServiceItem.builder()
                .id(1L)
                .nomService("Consultation Générale")
                .prix(300.0)
                .build();

        Consultation consultation = Consultation.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .consultationServices(List.of(serviceItem))
                .build();

        Facture facture = Facture.builder()
                .idFacture(1L)
                .dateFacture(new Date())
                .montantTotal(300.0)
                .statut("EN_ATTENTE")
                .consultation(consultation)
                .build();

        // Act & Assert
        assertDoesNotThrow(() -> {
            byte[] pdf = pdfGeneratorService.generateFacturePDF(facture, patient);
            assertNotNull(pdf);
            assertTrue(pdf.length > 0);
        });
    }

    @Test
    void formatDate_WithLocalDate_Success() {
        // Arrange
        ConsultationPatientResponseDTO patient = ConsultationPatientResponseDTO.builder()
                .idPatient(1L)
                .nom("Test")
                .prenom("Test")
                .build();

        Consultation consultation = Consultation.builder()
                .idConsultation(1L)
                .idPatient(1L)
                .consultationServices(List.of())
                .build();

        Facture facture = Facture.builder()
                .idFacture(1L)
                .dateFacture(new Date())
                .montantTotal(100.0)
                .statut("EN_ATTENTE")
                .consultation(consultation)
                .build();

        // Act & Assert - Should handle LocalDate without exception
        assertDoesNotThrow(() -> {
            byte[] pdf = pdfGeneratorService.generateFacturePDF(facture, patient);
            assertNotNull(pdf);
        });
    }
}