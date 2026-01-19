package com.example.consultationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "consultation_service")  
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationServiceItem {  
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_consultation")
    private Consultation consultation;

    private Long idService;
    private String nomService;
    private Double prix;
}