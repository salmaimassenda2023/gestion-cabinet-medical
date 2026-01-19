package com.example.patientservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dossiers_medicaux")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DossierMedical {

    @Id
    @Column(name = "patient_id")
    private Long id;

    @OneToOne
    @MapsId 
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "antecedents_medicaux", columnDefinition = "TEXT")
    private String antecedentsMedicaux;

    @Column(name = "antecedents_chirurgicaux", columnDefinition = "TEXT")
    private String antecedentsChirurgicaux;

    @Column(name = "allergies", columnDefinition = "TEXT")
    private String allergies;

    @Column(name = "groupe_sanguin", length = 5)
    private String groupeSanguin;

    @Column(name = "remarques", columnDefinition = "TEXT")
    private String remarques;

    @CreationTimestamp
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "dossierMedical", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentMedical> documents = new ArrayList<>();

    public void addDocument(DocumentMedical document) {
        documents.add(document);
        document.setDossierMedical(this);
    }

    public void removeDocument(DocumentMedical document) {
        documents.remove(document);
        document.setDossierMedical(null);
    }
}