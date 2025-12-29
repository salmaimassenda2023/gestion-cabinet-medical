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
public class OrdonnanceMedicament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrdonnance;
    private Date dateCreation;

    @ManyToOne
    @JoinColumn(name = "idConsultation")
    private Consultation consultation;

    @OneToMany(mappedBy = "ordonnanceMedicament", cascade = CascadeType.ALL)
    private List<LigneOrdonnanceMedicament> lignes;
}
