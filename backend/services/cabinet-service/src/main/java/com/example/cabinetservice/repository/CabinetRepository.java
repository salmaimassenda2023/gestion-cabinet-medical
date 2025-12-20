package com.example.cabinetservice.repository;

import com.example.cabinetservice.entity.Cabinet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CabinetRepository extends JpaRepository<Cabinet, Long> {
    Optional<Cabinet> findByMedecinId(Long medecinId);
    boolean existsByMedecinId(Long medecinId);
}