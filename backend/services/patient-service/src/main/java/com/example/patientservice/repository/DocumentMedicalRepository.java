package com.example.patientservice.repository;


import com.example.patientservice.entity.DocumentMedical;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentMedicalRepository extends JpaRepository<DocumentMedical, Long> {

    List<DocumentMedical> findByDossierMedical_PatientId(Long patientId);
}
