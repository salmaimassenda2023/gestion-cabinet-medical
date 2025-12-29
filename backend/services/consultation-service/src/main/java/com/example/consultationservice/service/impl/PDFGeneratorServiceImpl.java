package com.example.consultationservice.service.impl;

import com.example.consultationservice.dto.ConsultationPatientResponseDTO;
import com.example.consultationservice.entity.*;
import com.example.consultationservice.enums.TypeExamenSupplementaire;
import com.example.consultationservice.service.PDFGeneratorService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.apache.http.client.utils.DateUtils.formatDate;

/**
 * Service de génération de PDF
 * Responsabilité: Créer des documents PDF (Single Responsibility)
 * Utilise OpenPDF (iText fork) pour la génération
 */
@Service
@Slf4j
public class PDFGeneratorServiceImpl implements PDFGeneratorService {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter LOCAL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Génère un PDF pour une ordonnance de médicaments
     */
    @Override
    public byte[] generateOrdonnanceMedicamentPDF(OrdonnanceMedicament ordonnance,
                                                  ConsultationPatientResponseDTO patient) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // En-tête
            addHeader(document, "ORDONNANCE MÉDICALE");

            // Informations patient
            addPatientInfo(document, patient);

            // Date
            String formattedDate = formatDate(ordonnance.getDateCreation());
            Paragraph date = new Paragraph("Date: " + (formattedDate != null ? formattedDate : ""));
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(Chunk.NEWLINE);

            // Liste des médicaments
            document.add(new Paragraph("Prescription:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(Chunk.NEWLINE);

            int count = 1;
            for (LigneOrdonnanceMedicament ligne : ordonnance.getLignes()) {
                Paragraph med = new Paragraph();
                med.add(new Chunk(count + ". ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                med.add(new Chunk(ligne.getNomMedicament(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(med);

                document.add(new Paragraph("   Posologie: " + ligne.getPosologie()));
                document.add(new Paragraph("   Durée: " + ligne.getDuree()));
                document.add(Chunk.NEWLINE);
                count++;
            }

            // Signature
            addFooter(document);

            document.close();

            log.info("PDF ordonnance médicament généré: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF ordonnance médicament", e);
            throw new RuntimeException("Erreur de génération PDF", e);
        }
    }

    /**
     * Génère un PDF pour une ordonnance d'examens
     */
    @Override
    public byte[] generateOrdonnanceExamenPDF(OrdonnanceExamen ordonnance,
                                              ConsultationPatientResponseDTO patient) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // En-tête
            addHeader(document, "ORDONNANCE D'EXAMENS");

            // Informations patient
            addPatientInfo(document, patient);

            // Date
            Paragraph date = new Paragraph("Date: " + DATETIME_FORMAT.format(ordonnance.getDateCreation()));
            date.setAlignment(Element.ALIGN_RIGHT);
            document.add(date);
            document.add(Chunk.NEWLINE);

            // Liste des examens
            document.add(new Paragraph("Examens à réaliser:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(Chunk.NEWLINE);

            int count = 1;
            for (LigneOrdonnanceExamen ligne : ordonnance.getLignes()) {
                Paragraph exam = new Paragraph();
                exam.add(new Chunk(count + ". ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                exam.add(new Chunk(getExamenLabel(ligne.getTypeExamen()),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(exam);

                if (ligne.getDescription() != null && !ligne.getDescription().isEmpty()) {
                    document.add(new Paragraph("   Description: " + ligne.getDescription()));
                }
                document.add(Chunk.NEWLINE);
                count++;
            }

            // Signature
            addFooter(document);

            document.close();

            log.info("PDF ordonnance examen généré: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF ordonnance examen", e);
            throw new RuntimeException("Erreur de génération PDF", e);
        }
    }

    /**
     * Génère un PDF pour une facture
     */
    /**
     * Updated generateFacturePDF method for PDFGeneratorServiceImpl
     * This generates a comprehensive invoice with all services
     */
    @Override
    public byte[] generateFacturePDF(Facture facture, ConsultationPatientResponseDTO patient) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // En-tête
            addHeader(document, "FACTURE");

            // Numéro de facture
            Paragraph numero = new Paragraph("N° Facture: " + facture.getIdFacture(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            numero.setAlignment(Element.ALIGN_RIGHT);
            document.add(numero);

            Paragraph numeroConsult = new Paragraph("N° Consultation: " +
                    facture.getConsultation().getIdConsultation(),
                    FontFactory.getFont(FontFactory.HELVETICA, 10));
            numeroConsult.setAlignment(Element.ALIGN_RIGHT);
            document.add(numeroConsult);
            document.add(Chunk.NEWLINE);

            // Informations patient
            addPatientInfo(document, patient);

            // Date
            Paragraph date = new Paragraph("Date: " + DATE_FORMAT.format(facture.getDateFacture()));
            document.add(date);
            document.add(Chunk.NEWLINE);

            // Détails des services
            document.add(new Paragraph("Détails de la consultation:",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            document.add(Chunk.NEWLINE);

            // Create a simple table for services
            com.lowagie.text.Table table = new com.lowagie.text.Table(3);
            table.setWidth(100);
            table.setPadding(5);
            table.setSpacing(1);

            // Header row
            com.lowagie.text.Cell headerService = new com.lowagie.text.Cell(new Phrase("Service",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            headerService.setHeader(true);
            headerService.setBackgroundColor(new java.awt.Color(230, 230, 230));
            table.addCell(headerService);

            com.lowagie.text.Cell headerPrix = new com.lowagie.text.Cell(new Phrase("Prix (MAD)",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            headerPrix.setHeader(true);
            headerPrix.setBackgroundColor(new java.awt.Color(230, 230, 230));
            table.addCell(headerPrix);

            com.lowagie.text.Cell headerNomService = new com.lowagie.text.Cell(new Phrase("Nom de Service",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            headerNomService.setHeader(true);
            headerNomService.setBackgroundColor(new java.awt.Color(230, 230, 230));
            table.addCell(headerNomService);

            table.endHeaders();

            // Service rows
            for (ConsultationServiceItem service : facture.getConsultation().getConsultationServices()) {
                table.addCell(new Phrase(service.getNomService(),
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                table.addCell(new Phrase("1",
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
                table.addCell(new Phrase(String.format("%.2f", service.getPrix()),
                        FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            // Total
            Paragraph total = new Paragraph("TOTAL: " + String.format("%.2f", facture.getMontantTotal()) + " MAD",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16));
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);
            document.add(Chunk.NEWLINE);



            // Notes (if any)
            if (facture.getNotes() != null && !facture.getNotes().isEmpty()) {
                document.add(Chunk.NEWLINE);
                document.add(new Paragraph("Notes:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                document.add(new Paragraph(facture.getNotes(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            }

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            // Footer
            Paragraph footer = new Paragraph("Merci pour votre confiance",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            log.info("PDF facture généré: {} bytes", baos.size());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF facture", e);
            throw new RuntimeException("Erreur de génération PDF", e);
        }
    }    // ============ Méthodes utilitaires ============

    private void addHeader(Document document, String title) throws DocumentException {
        Paragraph header = new Paragraph(title,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);
    }

    private void addPatientInfo(Document document,
                                ConsultationPatientResponseDTO patient)
            throws DocumentException {

        document.add(new Paragraph("Patient:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
        document.add(new Paragraph("Nom: " + patient.getNom() + " " + patient.getPrenom()));
        document.add(new Paragraph("CIN: " + patient.getCin()));

        String formattedDate = formatDate(patient.getDateNaissance());
        if (formattedDate != null) {
            document.add(new Paragraph("Date de naissance: " + formattedDate));
        }

        if (patient.getTelephone() != null) {
            document.add(new Paragraph("Téléphone: " + patient.getTelephone()));
        }

        document.add(Chunk.NEWLINE);
    }

    private String formatDate(Object dateObj) {
        if (dateObj == null) {
            return null;
        }

        try {
            if (dateObj instanceof Date) {
                return DATE_FORMAT.format((Date) dateObj);
            }
            else if (dateObj instanceof LocalDate) {
                return ((LocalDate) dateObj).format(LOCAL_DATE_FORMATTER);
            }
            else if (dateObj instanceof LocalDateTime) {
                return ((LocalDateTime) dateObj).format(LOCAL_DATETIME_FORMATTER);
            }
            else {
                // For any other type, convert to string
                return dateObj.toString();
            }
        } catch (Exception e) {
            log.warn("Could not format date: {}", dateObj, e);
            return dateObj.toString();
        }
    }

    private void addFooter(Document document) throws DocumentException {
        document.add(Chunk.NEWLINE);
        document.add(Chunk.NEWLINE);

        Paragraph signature = new Paragraph("Signature et cachet du médecin",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12));
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);
    }

    private String getExamenLabel(TypeExamenSupplementaire type) {
        switch (type) {
            case BLOOD_TEST: return "Analyse sanguine";
            case ECG: return "Électrocardiogramme (ECG)";
            case XRAY: return "Radiographie";
            case MRI: return "IRM";
            case ULTRASOUND: return "Échographie";
            default: return type.name();
        }
    }

    private String getStatutLabel(String statut) {
        switch (statut) {
            case "EN_ATTENTE": return "En attente";
            case "PAYEE": return "Payée";
            case "ANNULEE": return "Annulée";
            default: return statut;
        }
    }
}