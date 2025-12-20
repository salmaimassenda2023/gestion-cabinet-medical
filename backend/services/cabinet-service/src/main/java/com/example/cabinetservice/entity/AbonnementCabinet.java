package com.example.cabinetservice.entity;

import com.example.cabinetservice.enums.AbonnementStatus;
import com.example.cabinetservice.enums.TypePeriode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonnements_cabinet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AbonnementCabinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abonnement")
    private Long idAbonnement;

    @Column(name = "date_debut", nullable = false)
    private LocalDateTime dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AbonnementStatus statut;

    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_periode", nullable = false)
    private TypePeriode typePeriode;

    @OneToOne
    @JoinColumn(name = "cabinet_id", referencedColumnName = "id", unique = true)
    private Cabinet cabinet;

}
