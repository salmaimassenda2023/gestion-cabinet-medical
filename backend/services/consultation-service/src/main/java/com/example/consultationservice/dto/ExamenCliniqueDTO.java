package com.example.consultationservice.dto;

import com.example.consultationservice.enums.TypeExamenClinique;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenCliniqueDTO {
    private Long idExamen;
    private TypeExamenClinique typeExamen;
    private String valeur;
    private String unite;
}