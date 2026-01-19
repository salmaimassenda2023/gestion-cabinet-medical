package com.example.cabinetservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cabinets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cabinet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String specialite;

    @Column(nullable = false)
    private String adresse;

    @Column(nullable = false)
    private String tel;

    @Column(columnDefinition = "TEXT")
    private String logo;

    @Column(name = "max_patients_jour")
    private Integer maxPatientsJour;

    @Column(name = "duree_consultation")
    private Integer dureeConsultation;

    private Long medecinId;

    @Builder.Default
    private Boolean actif = false;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "cabinet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AbonnementCabinet abonnement;

    @OneToMany(mappedBy = "cabinet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ServiceConsultation> services;

}