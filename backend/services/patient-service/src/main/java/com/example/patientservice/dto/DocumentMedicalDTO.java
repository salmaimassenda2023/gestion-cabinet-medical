package com.example.patientservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMedicalDTO {
    private Long idDocument;
    private String type;
    private String nom;
    private String url;
    private Long tailleOctets;
    private LocalDateTime dateUpload;
    private Long uploadedBy;
}