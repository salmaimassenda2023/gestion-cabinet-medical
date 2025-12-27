package com.example.consultationservice.repository;

import com.example.consultationservice.entity.OrdonnanceExamen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdonnanceExamenRepository extends JpaRepository<OrdonnanceExamen, Long> {
}
