package com.example.patientservice.repository;


import com.example.patientservice.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByCin(String cin);

    boolean existsByCin(String cin);

    List<Patient> findByIdCabinet(Long idCabinet);


    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.cin) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Patient> searchPatients(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Patient p WHERE p.idCabinet = :idCabinet AND (" +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.cin) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Patient> searchPatientsByCabinet(@Param("idCabinet") Long idCabinet,
                                          @Param("searchTerm") String searchTerm);
}