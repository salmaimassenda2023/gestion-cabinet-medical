package com.example.consultationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Africa/Casablanca")
    private Date dateCreation;

    private List<LigneOrdonnanceMedicamentDTO> lignes;
}