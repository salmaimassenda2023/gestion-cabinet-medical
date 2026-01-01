package com.example.rendezvousservice.dto;


import com.example.rendezvousservice.enums.MotifRendezVous;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
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

    @Future(message = "La date doit être dans le futur")
    private LocalDate dateRdv;

    private LocalTime heureRdv;

    private MotifRendezVous motif;


}
