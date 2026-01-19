package com.example.consultationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "facture")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facture")
    private Long idFacture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_consultation", nullable = false, unique = true)
    private Consultation consultation;

    @Column(name = "cabinet_id", nullable = true)
    private Long cabinetId;

    @Column(name = "montant_total", nullable = false)
    private Double montantTotal;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_facture", nullable = false)
    private Date dateFacture;

    @Column(name = "statut", nullable = false)
    private String statut; 

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @PrePersist
    protected void onCreate() {
        if (dateFacture == null) {
            dateFacture = new Date();
        }
        if (statut == null) {
            statut = "EN_ATTENTE";
        }
    }
}