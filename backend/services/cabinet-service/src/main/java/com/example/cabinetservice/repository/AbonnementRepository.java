package com.example.cabinetservice.repository;

import com.example.cabinetservice.entity.AbonnementCabinet;
import com.example.cabinetservice.enums.AbonnementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbonnementRepository extends JpaRepository<AbonnementCabinet, Long> {
    
    Optional<AbonnementCabinet> findByCabinetId(Long cabinetId);
    
    @Query("SELECT a FROM AbonnementCabinet a WHERE a.dateFin < :date AND a.statut = :status")
    List<AbonnementCabinet> findExpiringAbonnements(LocalDateTime date, AbonnementStatus status);

    List<AbonnementCabinet> findByStatut(AbonnementStatus statut);
}
