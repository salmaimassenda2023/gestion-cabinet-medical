package com.example.cabinetservice.repository;

import com.example.cabinetservice.entity.PaiementAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<PaiementAbonnement, Long> {
    List<PaiementAbonnement> findByAbonnementIdAbonnement(Long idAbonnement);
}
