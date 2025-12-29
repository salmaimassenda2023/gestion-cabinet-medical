package com.example.consultationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicamentDTO {
    private String id;
    private String nom;
    private String dci;
    private String forme;
    private String dosage;
}