package com.example.consultationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdonnanceMedicamentDTO {
    private Long idOrdonnance;
    private Date dateCreation;
    private Boolean signee;
    private List<LigneOrdonnanceMedicamentDTO> lignes;
}
