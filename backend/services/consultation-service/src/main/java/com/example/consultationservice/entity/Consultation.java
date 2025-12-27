package com.example.consultationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Consultation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConsultation;
    private Long idPatient; // Référence externe
    private Long idMedecin; // Référence externe
    private Long idRendezVous; // Référence externe
    private Date dateConsultation;
    private String diagnostic;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<ExamenClinique> examensCliniques;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<OrdonnanceMedicament> ordonnancesMedicaments;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<OrdonnanceExamen> ordonnancesExamens;

    @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL)
    private List<Facture> factures;
}
