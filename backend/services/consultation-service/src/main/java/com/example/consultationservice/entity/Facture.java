package com.example.consultationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFacture;
    private Date dateFacture;
    private Double montant;
    private String statut;

    @ManyToOne
    @JoinColumn(name = "idConsultation")
    private Consultation consultation;

    @ManyToOne
    @JoinColumn(name = "idService")
    private ServiceConsultation service;
}
