package com.example.rendezvousservice.dto;

import com.example.rendezvousservice.enums.MotifRendezVous;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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
public class UpdateRendezVousDTO {

    @NotNull(message = "La date est obligatoire")
    @FutureOrPresent(message = "La date doit être aujourd'hui ou dans le futur")
    private LocalDate dateRdv;

    @NotNull(message = "L'heure est obligatoire")
    private LocalTime heureRdv;

    @NotNull(message = "Le motif est obligatoire")
    private MotifRendezVous motif;
}