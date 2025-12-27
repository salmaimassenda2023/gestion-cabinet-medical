package com.example.consultationservice.entity;

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
public class LigneOrdonnanceMedicament {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idLigne;
    private String idMedicament; // Référence externe (MongoDB ID)
    private String nomMedicament;
    private String posologie;
    private String duree;

    @ManyToOne
    @JoinColumn(name = "idOrdonnance")
    private OrdonnanceMedicament ordonnanceMedicament;
}
