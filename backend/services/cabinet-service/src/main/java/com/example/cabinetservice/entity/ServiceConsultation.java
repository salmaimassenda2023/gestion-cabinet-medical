package com.example.cabinetservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "services_consultation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConsultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_service")
    private Long idService;

    @Column(name = "nom_service", nullable = false)
    private String nomService;

    private String description;

    @Column(nullable = false)
    private Double prix;

    @Builder.Default
    private Boolean obligatoire = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cabinet_id")
    private Cabinet cabinet;
}
