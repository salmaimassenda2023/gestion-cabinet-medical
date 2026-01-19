package com.example.cabinetservice.service;

import com.example.cabinetservice.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface CabinetService {
    CabinetResponseDTO createCabinet(CabinetCreateDTO dto);

    CabinetResponseDTO getCabinet(Long id);

    CabinetResponseDTO updateCabinet(Long id, CabinetUpdateDTO dto);

    void deleteCabinet(Long id);

    CabinetResponseDTO getCabinetByMedecinId(Long medecinId);

    Boolean isCabinetActive(Long id);

    ServiceConsultationDTO addService(Long cabinetId, ServiceConsultationDTO dto);

    List<ServiceConsultationDTO> getServices(Long cabinetId);

    ServiceConsultationDTO getServiceById(Long cabinetId, Long serviceId);

    List<CabinetResponseDTO> getAllCabinets();

    @Transactional(readOnly = true)
    CabinetResponseDTO getCabinetByUserId(Long userId);

    List<AbonnementResponseDTO> getAllAbonnements();

    void handleSuccessfulPayment(Long abonnementId, Double amount, String stripePaymentId);

    @Transactional(readOnly = true)
    CabinetResponseDTO getCabinetForUser(Long userId);
}