package com.example.consultationservice.repository;

import com.example.consultationservice.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /**
     * Récupérer toutes les consultations d'un patient triées par date décroissante
     */
    List<Consultation> findByIdPatientOrderByDateConsultationDesc(Long idPatient);

    /**
     * Rechercher les consultations d'un patient entre deux dates
     */
    List<Consultation> findByIdPatientAndDateConsultationBetween(
            Long idPatient, Date dateDebut, Date dateFin);

    List<Consultation> findByIdCabinetOrderByDateConsultationDesc(Long idCabinet);
}