package com.example.consultationservice.repository;

import com.example.consultationservice.entity.ExamenClinique;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamenCliniqueRepository extends JpaRepository<ExamenClinique, Long> {
}
