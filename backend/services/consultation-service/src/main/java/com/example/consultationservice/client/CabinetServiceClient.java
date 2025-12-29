package com.example.consultationservice.client;

import com.example.consultationservice.dto.ServiceConsultationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Client Feign pour communiquer avec le Cabinet Service
 * Récupère les informations des services de consultation
 */
@FeignClient(name = "cabinet-service", path = "/api/cabinet")
public interface CabinetServiceClient {

    @GetMapping("/{id}/services")
    List<ServiceConsultationDTO> getServices(@PathVariable Long id);

    @GetMapping("/{cabinetId}/services/{serviceId}")
    ServiceConsultationDTO getServiceById(
            @PathVariable("cabinetId") Long cabinetId,
            @PathVariable("serviceId") Long serviceId);
}