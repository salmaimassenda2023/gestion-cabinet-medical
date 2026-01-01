package com.example.rendezvousservice.dto;

import com.example.rendezvousservice.enums.MotifRendezVous;
import com.example.rendezvousservice.enums.StatutRendezVous;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousDTO {
    private Long id;
    private Long idPatient;
    private Long idMedecin;
    private LocalDate dateRdv;
    private LocalTime heureRdv;
    private MotifRendezVous motif;
    private StatutRendezVous statut;
    private Integer ordrePassage;
    private LocalDateTime heureArrivee;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
