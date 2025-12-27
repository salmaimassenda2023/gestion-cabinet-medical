package org.example.controller;

import org.example.entity.Medicament;
import org.example.service.MedicamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicament")
@CrossOrigin(origins = "*") // For frontend access
public class MedicamentController {

    @Autowired
    private MedicamentService medicamentService;

    @GetMapping
    public List<Medicament> getAllMedicaments() {
        return medicamentService.findAll();
    }

    @GetMapping("/search")
    public List<Medicament> searchMedicaments(@RequestParam String term) {
        return medicamentService.searchMedicaments(term);
    }

    @GetMapping("/autocomplete")
    public List<Medicament> autocomplete(@RequestParam String prefix) {
        return medicamentService.searchByNomStartingWith(prefix);
    }
}