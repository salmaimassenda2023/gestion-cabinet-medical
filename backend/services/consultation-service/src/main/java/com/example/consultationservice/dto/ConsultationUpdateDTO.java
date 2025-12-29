package com.example.consultationservice.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationUpdateDTO {
    @Size(max = 2000, message = "Le diagnostic ne doit pas dépasser 2000 caractères")
    private String diagnostic;
}