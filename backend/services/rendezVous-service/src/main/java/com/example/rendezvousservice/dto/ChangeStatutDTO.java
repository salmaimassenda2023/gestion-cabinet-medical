package com.example.rendezvousservice.dto;


import com.example.rendezvousservice.enums.StatutRendezVous;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStatutDTO {

    @NotNull(message = "Le statut est obligatoire")
    private StatutRendezVous statut;

    private String remarques;
}
