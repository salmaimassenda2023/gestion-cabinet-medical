package com.example.consultationservice.client;

import com.example.consultationservice.dto.ConsultationPatientResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "patient-service", path = "/api/patient")
public interface PatientServiceClient {

    @GetMapping("/{id}")
    public ConsultationPatientResponseDTO getPatient(@PathVariable Long id);


}
