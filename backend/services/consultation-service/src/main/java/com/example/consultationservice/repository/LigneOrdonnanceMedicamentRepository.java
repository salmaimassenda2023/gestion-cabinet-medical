package com.example.consultationservice.repository;

import com.example.consultationservice.entity.LigneOrdonnanceMedicament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneOrdonnanceMedicamentRepository extends JpaRepository<LigneOrdonnanceMedicament, Long> {
}
