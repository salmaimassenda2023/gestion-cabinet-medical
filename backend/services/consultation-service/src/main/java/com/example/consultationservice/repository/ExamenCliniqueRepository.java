package com.example.consultationservice.repository;

import com.example.consultationservice.entity.ExamenClinique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamenCliniqueRepository extends JpaRepository<ExamenClinique, Long> {

    /**
     * Récupérer tous les examens cliniques d'une consultation
     */
    List<ExamenClinique> findByConsultation_IdConsultation(Long idConsultation);
}
