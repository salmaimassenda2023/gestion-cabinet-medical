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
public class OrdonnanceExamen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrdonnance;
    private Date dateCreation;
    private Boolean signee;

    @ManyToOne
    @JoinColumn(name = "idConsultation")
    private Consultation consultation;

    @OneToMany(mappedBy = "ordonnanceExamen", cascade = CascadeType.ALL)
    private List<LigneOrdonnanceExamen> lignes;
}
