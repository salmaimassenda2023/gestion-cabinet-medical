package com.example.utilisateurservice.controller;

import com.example.utilisateurservice.dto.*;
import com.example.utilisateurservice.entity.Utilisateur;
import com.example.utilisateurservice.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/utilisateur/users")
@RequiredArgsConstructor
@Slf4j
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    // ============================================================
    // ENDPOINT DE BOOTSTRAP - À UTILISER UNE SEULE FOIS (PUBLIC)
    // ============================================================
    @PostMapping("/bootstrap/superadmin")
    public ResponseEntity<UtilisateurResponse> bootstrapSuperAdmin(@Valid @RequestBody UtilisateurRequest request) {
        log.info("🚀 Bootstrap SUPER_ADMIN");

        // Vérifier qu'aucun SUPER_ADMIN n'existe déjà
        List<UtilisateurResponse> superAdmins = utilisateurService.getUtilisateursByRole(Utilisateur.Role.SUPER_ADMIN);
        if (!superAdmins.isEmpty()) {
            throw new RuntimeException("Un SUPER_ADMIN existe déjà. Endpoint désactivé.");
        }

        // Forcer le rôle SUPER_ADMIN
        request.setRole(Utilisateur.Role.valueOf("SUPER_ADMIN"));

        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createUtilisateur(request));
    }

    // ============================================================
    // CRUD UTILISATEURS
    // ============================================================

    /**
     * Créer un utilisateur
     * Accessible par: SUPER_ADMIN et ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<UtilisateurResponse> createUtilisateur(@Valid @RequestBody UtilisateurRequest request) {
        log.info("Création utilisateur: {}", request.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.createUtilisateur(request));
    }

    /**
     * Récupérer l'utilisateur connecté
     * Accessible par: Tous les utilisateurs authentifiés
     */
    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponse> getCurrentUser() {
        log.info("Récupération utilisateur connecté");
        return ResponseEntity.ok(utilisateurService.getCurrentUser());
    }

    /**
     * Récupérer un utilisateur par ID
     * Accessible par: SUPER_ADMIN, ADMIN et MEDECIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MEDECIN')")
    public ResponseEntity<UtilisateurResponse> getUtilisateurById(@PathVariable Long id) {
        log.info("Récupération utilisateur ID: {}", id);
        return ResponseEntity.ok(utilisateurService.getUtilisateurById(id));
    }

    /**
     * Lister tous les utilisateurs
     * Accessible par: SUPER_ADMIN et ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UtilisateurResponse>> getAllUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return ResponseEntity.ok(utilisateurService.getAllUtilisateurs());
    }

    /**
     * Lister les utilisateurs par rôle
     * Accessible par: SUPER_ADMIN et ADMIN
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<List<UtilisateurResponse>> getUtilisateursByRole(@PathVariable Utilisateur.Role role) {
        log.info("Récupération utilisateurs par rôle: {}", role);
        return ResponseEntity.ok(utilisateurService.getUtilisateursByRole(role));
    }

    /**
     * Lister les utilisateurs par cabinet
     * Accessible par: SUPER_ADMIN, ADMIN et MEDECIN
     */
    @GetMapping("/cabinet/{idCabinet}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'MEDECIN')")
    public ResponseEntity<List<UtilisateurResponse>> getUtilisateursByCabinet(@PathVariable Long idCabinet) {
        log.info("Récupération utilisateurs du cabinet: {}", idCabinet);
        return ResponseEntity.ok(utilisateurService.getUtilisateursByCabinet(idCabinet));
    }

    /**
     * Supprimer un utilisateur
     * Accessible par: SUPER_ADMIN et ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteUtilisateur(@PathVariable Long id) {
        log.info("Suppression utilisateur: {}", id);
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }
    /**
     * Mettre à jour un utilisateur
     * Accessible par: SUPER_ADMIN, ADMIN, ou l'utilisateur lui-même
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN') or @utilisateurService.isCurrentUser(#id)")
    public ResponseEntity<UtilisateurResponse> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateRequest request) {
        log.info("Mise à jour utilisateur ID: {}", id);
        return ResponseEntity.ok(utilisateurService.updateUtilisateur(id, request));
    }

    /**
     * Changer son propre mot de passe
     * Accessible par: Tous les utilisateurs authentifiés (pour leur propre compte)
     */
    @PutMapping("/{id}/password")
    @PreAuthorize("@utilisateurService.isCurrentUser(#id)")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordUpdateRequest request) {
        log.info("Changement de mot de passe pour utilisateur ID: {}", id);
        utilisateurService.changePassword(id, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur (par admin)
     * Accessible par: SUPER_ADMIN et ADMIN uniquement
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody AdminPasswordResetRequest request) {
        log.info("Réinitialisation du mot de passe pour utilisateur ID: {} (par admin)", id);
        utilisateurService.resetPassword(id, request);
        return ResponseEntity.noContent().build();
    }
}