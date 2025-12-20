package com.example.cabinetservice.repository;

import com.example.cabinetservice.entity.ServiceConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceConsultationRepository extends JpaRepository<ServiceConsultation, Long> {
    List<ServiceConsultation> findByCabinetId(Long cabinetId);
}
