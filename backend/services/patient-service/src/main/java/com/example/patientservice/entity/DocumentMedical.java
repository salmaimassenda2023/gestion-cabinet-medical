package com.example.patientservice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents_medicaux")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMedical {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Long idDocument;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_medical_id", nullable = false)  // ✅ Référence maintenant patient_id
    private DossierMedical dossierMedical;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String url;

    @Column(name = "taille_octets")
    private Long tailleOctets;


}