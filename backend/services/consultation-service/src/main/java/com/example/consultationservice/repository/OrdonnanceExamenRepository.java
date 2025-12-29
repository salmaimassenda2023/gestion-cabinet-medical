package com.example.consultationservice.repository;

import com.example.consultationservice.entity.OrdonnanceExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdonnanceExamenRepository extends JpaRepository<OrdonnanceExamen, Long> {

    /**
     * Récupérer toutes les ordonnances examens d'une consultation
     */
    List<OrdonnanceExamen> findByConsultation_IdConsultation(Long idConsultation);
}
