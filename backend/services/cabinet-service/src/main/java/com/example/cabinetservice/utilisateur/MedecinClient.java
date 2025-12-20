package com.example.cabinetservice.utilisateur;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "utilisateur-service", url = "http://localhost:8082")  // ← Port 8082, pas 8083
public interface MedecinClient {

    @GetMapping("/api/utilisateur/users/{id}")  // ← Bon chemin
    UtilisateurResponse getUtilisateurById(@PathVariable("id") Long id);

    @PutMapping("/api/utilisateur/users/{id}/cabinet")  // ← Utiliser PUT au lieu de PATCH
    void updateCabinetId(@PathVariable("id") Long id, @RequestParam("idCabinet") Long idCabinet);
}