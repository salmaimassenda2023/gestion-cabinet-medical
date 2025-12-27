package com.example.consultationservice.client;

import com.example.consultationservice.dto.MedicamentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "medicament-service", path = "/api/medicaments")
public interface MedicamentServiceClient {

    @GetMapping("/search")
    List<MedicamentDTO> searchMedicaments(@RequestParam("term") String term);

    @GetMapping("/autocomplete")
    List<MedicamentDTO> autocomplete(@RequestParam("prefix") String prefix);

    // If we need to fetch by ID
    // @GetMapping("/{id}")
    // MedicamentDTO getMedicamentById(@PathVariable("id") String id);
}
