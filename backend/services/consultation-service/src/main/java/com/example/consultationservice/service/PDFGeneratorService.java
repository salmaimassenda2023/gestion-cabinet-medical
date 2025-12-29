package com.example.consultationservice.service;

import com.example.consultationservice.dto.ConsultationPatientResponseDTO;
import com.example.consultationservice.entity.Facture;
import com.example.consultationservice.entity.OrdonnanceExamen;
import com.example.consultationservice.entity.OrdonnanceMedicament;

import javax.swing.text.Document;

public interface PDFGeneratorService {

    public byte[] generateOrdonnanceMedicamentPDF(OrdonnanceMedicament ordonnance,
                                                  ConsultationPatientResponseDTO patient);
    public byte[] generateOrdonnanceExamenPDF(OrdonnanceExamen ordonnance,
                                              ConsultationPatientResponseDTO patient);
    public byte[] generateFacturePDF(Facture facture,
                                     ConsultationPatientResponseDTO patient);
}
