package com.example.consultationservice.repository;

import com.example.consultationservice.entity.LigneOrdonnanceExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneOrdonnanceExamenRepository extends JpaRepository<LigneOrdonnanceExamen, Long> {
}
