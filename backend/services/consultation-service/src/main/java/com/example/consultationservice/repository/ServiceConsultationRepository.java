package com.example.consultationservice.repository;

import com.example.consultationservice.entity.ServiceConsultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceConsultationRepository extends JpaRepository<ServiceConsultation, Long> {
}
