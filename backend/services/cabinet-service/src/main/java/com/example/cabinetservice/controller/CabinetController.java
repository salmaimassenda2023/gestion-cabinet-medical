package com.example.cabinetservice.controller;

import com.example.cabinetservice.dto.*;
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

    @GetMapping
    public ResponseEntity<List<CabinetResponseDTO>> getAllCabinets() {
        return ResponseEntity.ok(cabinetService.getAllCabinets());
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

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<CabinetResponseDTO> getCabinetByMedecinId(@PathVariable Long medecinId) {
        return ResponseEntity.ok(cabinetService.getCabinetByMedecinId(medecinId));
    }

    @GetMapping("/{id}/actif")
    public ResponseEntity<Boolean> isCabinetActive(@PathVariable Long id) {
        return ResponseEntity.ok(cabinetService.isCabinetActive(id));
    }

    @PostMapping("/{id}/services")
    public ResponseEntity<ServiceConsultationDTO> addService(@PathVariable Long id,
            @RequestBody @Valid ServiceConsultationDTO dto) {
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
    @GetMapping("/abonnements")
    public ResponseEntity<List<AbonnementResponseDTO>> getAllAbonnements() {
        return ResponseEntity.ok(cabinetService.getAllAbonnements());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CabinetResponseDTO> getCabinetByUserId(@PathVariable Long userId) {
        try {
            CabinetResponseDTO cabinet = cabinetService.getCabinetByUserId(userId);
            return ResponseEntity.ok(cabinet);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

}
