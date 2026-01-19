package com.example.rendezvousservice.entity;

import com.example.rendezvousservice.enums.MotifRendezVous;
import com.example.rendezvousservice.enums.StatutRendezVous;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "rendezvous")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_patient", nullable = false)
    private Long idPatient;

    @Column(name = "id_medecin", nullable = false)
    private Long idMedecin;

    @Column(name = "date_rdv", nullable = false)
    private LocalDate dateRdv;

    @Column(name = "heure_rdv", nullable = false)
    private LocalTime heureRdv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MotifRendezVous motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatutRendezVous statut;

    @Column(name = "ordre_passage")
    private Integer ordrePassage;

    @Column(name = "heure_arrivee")
    private LocalDateTime heureArrivee;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (statut == null) {
            statut = StatutRendezVous.CONFIRME;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    public boolean peutEtreModifie() {
        return statut != StatutRendezVous.TERMINE &&
                statut != StatutRendezVous.ANNULE;
    }

    public boolean peutEtreAnnule() {
        return statut != StatutRendezVous.TERMINE &&
                statut != StatutRendezVous.ANNULE;
    }

    public boolean peutEtreAjouteEnAttente() {
        return statut == StatutRendezVous.CONFIRME ||
                statut == StatutRendezVous.PLANIFIE;
    }

    public void ajouterEnListeAttente(int ordre) {
        if (!peutEtreAjouteEnAttente()) {
            throw new IllegalStateException(
                    "Ce rendez-vous ne peut pas être ajouté à la liste d'attente (statut: " + statut + ")"
            );
        }
        this.ordrePassage = ordre;
        this.heureArrivee = LocalDateTime.now();
        this.statut = StatutRendezVous.PRESENT;
    }

    public void retirerDeListeAttente() {
        this.ordrePassage = null;
        this.heureArrivee = null;
        this.statut = StatutRendezVous.CONFIRME;
    }

    public void passerEnConsultation() {
        if (statut != StatutRendezVous.PRESENT) {
            throw new IllegalStateException(
                    "Le patient doit être présent pour passer en consultation (statut actuel: " + statut + ")"
            );
        }
        this.statut = StatutRendezVous.EN_CONSULTATION;
    }

    public void annuler() {
        if (!peutEtreAnnule()) {
            throw new IllegalStateException(
                    "Ce rendez-vous ne peut pas être annulé (statut: " + statut + ")"
            );
        }
        this.statut = StatutRendezVous.ANNULE;
    }
}