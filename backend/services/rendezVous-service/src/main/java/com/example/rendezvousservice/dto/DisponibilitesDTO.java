package com.example.rendezvousservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilitesDTO {
    private Long idMedecin;
    private LocalDate date;
    private List<CreneauDTO> creneaux;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreneauDTO {
        private LocalTime heure;
        private boolean disponible;
    }
}
