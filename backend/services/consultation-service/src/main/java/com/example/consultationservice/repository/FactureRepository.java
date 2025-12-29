package com.example.consultationservice.repository;

import com.example.consultationservice.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FactureRepository extends JpaRepository<Facture, Long> {

    /**
     * Récupérer toutes les factures d'une consultation
     */
    List<Facture> findByConsultation_IdConsultation(Long idConsultation);

    /**
     * Récupérer toutes les factures d'un patient
     */
    List<Facture> findByConsultation_IdPatient(Long idPatient);

    /**
     * Récupérer les factures par statut
     */
    List<Facture> findByStatut(String statut);
}