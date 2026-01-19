package com.example.consultationservice.repository;

import com.example.consultationservice.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Récupérer toutes les factures d'un cabinet
     * Ordered by date DESC (most recent first)
     */
    List<Facture> findByCabinetIdOrderByDateFactureDesc(Long cabinetId);

    /**
     * Récupérer les factures d'un cabinet par statut
     */
    List<Facture> findByCabinetIdAndStatutOrderByDateFactureDesc(Long cabinetId, String statut);

    /**
     * Custom query to get factures with full consultation details
     */
    @Query("SELECT f FROM Facture f " +
            "LEFT JOIN FETCH f.consultation c " +
            "LEFT JOIN FETCH c.consultationServices " +
            "WHERE f.cabinetId = :cabinetId " +
            "ORDER BY f.dateFacture DESC")
    List<Facture> findByCabinetIdWithDetails(@Param("cabinetId") Long cabinetId);
}