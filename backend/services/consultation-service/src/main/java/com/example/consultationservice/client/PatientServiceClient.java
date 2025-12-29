package com.example.consultationservice.client;

import com.example.consultationservice.dto.ConsultationPatientResponseDTO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "patient-service", path = "/api/patient")
public interface PatientServiceClient {

    @GetMapping("/{id}")
    public ConsultationPatientResponseDTO getPatient(@PathVariable Long id);


}
