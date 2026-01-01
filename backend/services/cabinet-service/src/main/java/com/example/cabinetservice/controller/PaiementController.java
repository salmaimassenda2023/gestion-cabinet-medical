package com.example.cabinetservice.controller;

import com.example.cabinetservice.dto.PaiementResponseDTO;
import com.example.cabinetservice.service.CabinetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cabinet/paiement")
@RequiredArgsConstructor
public class PaiementController {

    private final CabinetService cabinetService;

    @GetMapping
    public ResponseEntity<List<PaiementResponseDTO>> getAllPaiements() {
        return ResponseEntity.ok(cabinetService.getAllPaiements());
    }
}
