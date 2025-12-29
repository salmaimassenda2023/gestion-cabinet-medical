package com.example.cabinetservice.service;

import com.example.cabinetservice.dto.CabinetCreateDTO;
import com.example.cabinetservice.dto.CabinetResponseDTO;
import com.example.cabinetservice.dto.CabinetUpdateDTO;
import com.example.cabinetservice.dto.ServiceConsultationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CabinetService {
    CabinetResponseDTO createCabinet(CabinetCreateDTO dto);

    CabinetResponseDTO getCabinet(Long id);

    CabinetResponseDTO updateCabinet(Long id, CabinetUpdateDTO dto);

    void deleteCabinet(Long id);

    Boolean isCabinetActive(Long id);

    ServiceConsultationDTO addService(Long cabinetId, ServiceConsultationDTO dto);

    List<ServiceConsultationDTO> getServices(Long cabinetId);

    ServiceConsultationDTO getServiceById(Long cabinetId,Long serviceId);

    void handleSuccessfulPayment(Long abonnementId, Double amount, String stripePaymentId);
}
