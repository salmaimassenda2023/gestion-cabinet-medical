package com.example.cabinetservice.entity;

import com.example.cabinetservice.enums.PaiementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "paiements_abonnement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaiementAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long idPaiement;

    @CreationTimestamp
    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(nullable = false)
    private Double montant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaiementStatus statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonnement_id")
    private AbonnementCabinet abonnement;

}