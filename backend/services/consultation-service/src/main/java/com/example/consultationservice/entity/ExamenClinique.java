package com.example.consultationservice.entity;

import com.example.consultationservice.enums.TypeExamenClinique;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamenClinique {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idExamen;

    @Enumerated(EnumType.STRING)
    private TypeExamenClinique typeExamen;

    private String valeur;
    private String unite;

    @ManyToOne
    @JoinColumn(name = "idConsultation")
    private Consultation consultation;
}
