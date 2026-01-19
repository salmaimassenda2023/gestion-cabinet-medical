package com.example.patientservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String cin;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    @Column(nullable = false, length = 1)
    private String sexe; 

    @Column(name = "telephone", length = 20)
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "adresse", columnDefinition = "TEXT")
    private String adresse;

    @Column(name = "type_mutuelle")
    private String typeMutuelle;

    @Column(name = "numero_mutuelle")
    private String numeroMutuelle;

    // Référence externe au cabinet
    @Column(name = "id_cabinet", nullable = false)
    private Long idCabinet;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relation bidirectionnelle avec DossierMedical
    @OneToOne(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private DossierMedical dossierMedical;
}