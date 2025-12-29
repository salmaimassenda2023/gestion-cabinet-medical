package com.example.consultationservice.repository;

import com.example.consultationservice.entity.OrdonnanceMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdonnanceMedicamentRepository extends JpaRepository<OrdonnanceMedicament, Long> {

    /**
     * Récupérer toutes les ordonnances médicaments d'une consultation
     */
    List<OrdonnanceMedicament> findByConsultation_IdConsultation(Long idConsultation);
}
