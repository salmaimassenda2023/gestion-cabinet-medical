package com.example.cabinetservice.controller;

import com.example.cabinetservice.dto.CabinetCreateDTO;
import com.example.cabinetservice.dto.CabinetResponseDTO;
import com.example.cabinetservice.dto.CabinetUpdateDTO;
import com.example.cabinetservice.dto.ServiceConsultationDTO;
import com.example.cabinetservice.service.CabinetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cabinet")
@RequiredArgsConstructor
public class CabinetController {

    private final CabinetService cabinetService;

    @PostMapping
    public ResponseEntity<CabinetResponseDTO> createCabinet(@RequestBody @Valid CabinetCreateDTO dto) {
        return new ResponseEntity<>(cabinetService.createCabinet(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CabinetResponseDTO> getCabinet(@PathVariable Long id) {
        return ResponseEntity.ok(cabinetService.getCabinet(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CabinetResponseDTO> updateCabinet(@PathVariable Long id, @RequestBody CabinetUpdateDTO dto) {
        return ResponseEntity.ok(cabinetService.updateCabinet(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCabinet(@PathVariable Long id) {
        cabinetService.deleteCabinet(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/actif")
    public ResponseEntity<Boolean> isCabinetActive(@PathVariable Long id) {
        return ResponseEntity.ok(cabinetService.isCabinetActive(id));
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<ServiceConsultationDTO> addService(@PathVariable Long id, @RequestBody @Valid ServiceConsultationDTO dto) {
        return new ResponseEntity<>(cabinetService.addService(id, dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/services")
    public ResponseEntity<List<ServiceConsultationDTO>> getServices(@PathVariable Long id) {
        return ResponseEntity.ok(cabinetService.getServices(id));
    }

    @GetMapping("/{cabinetId}/services/{serviceId}")
    public ResponseEntity<ServiceConsultationDTO> getServiceById(
            @PathVariable Long cabinetId,
            @PathVariable Long serviceId) {
        ServiceConsultationDTO service = cabinetService.getServiceById(cabinetId, serviceId);
        return ResponseEntity.ok(service);
    }
}
