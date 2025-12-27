package com.example.consultationservice.entity;

import com.example.consultationservice.enums.TypeExamenSupplementaire;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdonnanceExamen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigne;

    @Enumerated(EnumType.STRING)
    private TypeExamenSupplementaire typeExamen;

    private String description;

    @ManyToOne
    @JoinColumn(name = "idOrdonnance")
    private OrdonnanceExamen ordonnanceExamen;
}
