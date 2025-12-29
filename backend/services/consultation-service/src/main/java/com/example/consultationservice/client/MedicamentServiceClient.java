package com.example.consultationservice.client;

import com.example.consultationservice.dto.OrdonnanceMedicamentDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "medicament-service", path = "/api/medicaments")
public interface MedicamentServiceClient {

    @GetMapping("/search")
    List<OrdonnanceMedicamentDTO> searchMedicaments(@RequestParam("term") String term);

    @GetMapping("/autocomplete")
    List<OrdonnanceMedicamentDTO> autocomplete(@RequestParam("prefix") String prefix);

}
