package com.example.rendezvousservice.dto;


import com.example.rendezvousservice.enums.MotifRendezVous;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRendezVousDTO {

    @NotNull(message = "L'ID du patient est obligatoire")
    private Long idPatient;

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long idMedecin;

    @NotNull(message = "La date du rendez-vous est obligatoire")
    @Future(message = "La date doit être dans le futur")
    private LocalDate dateRdv;

    @NotNull(message = "L'heure du rendez-vous est obligatoire")
    private LocalTime heureRdv;

    @NotNull(message = "Le motif est obligatoire")
    private MotifRendezVous motif;


}