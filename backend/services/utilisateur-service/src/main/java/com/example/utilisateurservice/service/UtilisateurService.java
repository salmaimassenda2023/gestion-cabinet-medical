package com.example.utilisateurservice.service;

import com.example.utilisateurservice.dto.*;
import com.example.utilisateurservice.entity.Utilisateur;
import com.example.utilisateurservice.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakService keycloakService;

    @Transactional
    public UtilisateurResponse createUtilisateur(UtilisateurRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getLogin());

        // VALIDATION 1 : idCabinet obligatoire UNIQUEMENT pour MEDECIN et SECRETAIRE
        if ((request.getRole() == Utilisateur.Role.MEDECIN || request.getRole() == Utilisateur.Role.SECRETAIRE)
                && request.getIdCabinet() == null) {
            throw new IllegalArgumentException(
                    "Le champ idCabinet est obligatoire pour les rôles MEDECIN et SECRETAIRE");
        }

        // VALIDATION 2 : SUPER_ADMIN et ADMIN ne doivent PAS avoir de cabinet
        if ((request.getRole() == Utilisateur.Role.SUPER_ADMIN || request.getRole() == Utilisateur.Role.ADMIN)
                && request.getIdCabinet() != null) {
            log.warn("idCabinet ignoré pour {} (valeur reçue: {})", request.getRole(), request.getIdCabinet());
            request.setIdCabinet(null); // Forcer à NULL
        }

        // Vérifier si le login existe déjà
        if (utilisateurRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Un utilisateur avec ce login existe déjà");
        }

        // Créer l'utilisateur dans Keycloak
        String keycloakId = keycloakService.createUser(
                request.getLogin(),
                request.getPassword(),
                request.getPrenom(),
                request.getNom(),
                request.getLogin() + "@cabinet.ma",
                request.getRole());

        // Créer l'utilisateur dans la base de données
        Utilisateur utilisateur = Utilisateur.builder()
                .keycloakId(keycloakId)
                .login(request.getLogin())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .numTel(request.getNumTel())
                .signature(request.getSignature())
                .role(request.getRole())
                .idCabinet(request.getIdCabinet()) // NULL pour SUPER_ADMIN et ADMIN
                .actif(true)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        log.info("✅ Utilisateur créé avec succès: {} - Rôle: {} - Cabinet: {}",
                utilisateur.getIdUtilisateur(),
                utilisateur.getRole(),
                utilisateur.getIdCabinet() == null ? "TOUS LES CABINETS" : utilisateur.getIdCabinet());

        return mapToResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse registerMedecin(UtilisateurRequest request) {
        log.info("Inscription publique d'un nouveau médecin: {}", request.getLogin());

        // Forcer le rôle MEDECIN
        request.setRole(Utilisateur.Role.MEDECIN);

        // Validation spécifique
        if (utilisateurRepository.existsByLogin(request.getLogin())) {
            throw new RuntimeException("Un utilisateur avec ce login existe déjà");
        }

        // Créer l'utilisateur dans Keycloak
        String keycloakId = keycloakService.createUser(
                request.getLogin(),
                request.getPassword(),
                request.getPrenom(),
                request.getNom(),
                request.getLogin() + "@cabinet.ma",
                request.getRole());

        // Créer l'utilisateur dans la base de données
        Utilisateur utilisateur = Utilisateur.builder()
                .keycloakId(keycloakId)
                .login(request.getLogin())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .numTel(request.getNumTel())
                .signature(request.getSignature())
                .role(request.getRole())
                .actif(true)
                .build();

        utilisateur = utilisateurRepository.save(utilisateur);

        log.info("Médecin inscrit avec succès: {}", utilisateur.getIdUtilisateur());
        return mapToResponse(utilisateur);
    }

    @Transactional
    public void updateCabinetId(Long idUtilisateur, Long idCabinet) {
        log.info("Mise à jour du cabinet ID: {} pour l'utilisateur ID: {}", idCabinet, idUtilisateur);

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + idUtilisateur));

        // Update DB
        utilisateur.setIdCabinet(idCabinet);
        utilisateurRepository.save(utilisateur);
    }

    public UtilisateurResponse getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));
        return mapToResponse(utilisateur);
    }

    public List<UtilisateurResponse> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<UtilisateurResponse> getUtilisateursByRole(Utilisateur.Role role) {
        return utilisateurRepository.findByRole(role).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<UtilisateurResponse> getUtilisateursByCabinet(Long idCabinet) {
        log.info("Récupération des utilisateurs du cabinet: {}", idCabinet);

        try {
            List<Utilisateur> utilisateurs = utilisateurRepository.findByIdCabinet(idCabinet);
            log.info("Found {} users", utilisateurs.size());

            List<UtilisateurResponse> responses = new ArrayList<>();

            for (Utilisateur user : utilisateurs) {
                try {
                    UtilisateurResponse response = mapToResponse(user);
                    responses.add(response);
                } catch (Exception e) {
                    log.error("Error mapping user ID {}: {}", user.getIdUtilisateur(), e.getMessage());
                    log.error("User data: {}", user);
                    throw e; // Re-throw to see the exact error
                }
            }

            return responses;

        } catch (Exception e) {
            log.error("CRITICAL ERROR in getUtilisateursByCabinet for cabinet {}: {}",
                    idCabinet, e.getMessage(), e);
            throw new RuntimeException("Failed to get users: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteUtilisateur(Long id) {
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);

        // ADD AUTHORIZATION CHECK

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Additional safety check: Prevent self-deletion
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = jwt.getSubject();
        Utilisateur currentUser = utilisateurRepository.findByKeycloakId(currentUserKeycloakId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getIdUtilisateur().equals(id)) {
            log.error("User attempted to delete themselves: {}", id);
            throw new IllegalArgumentException("Cannot delete your own account. Contact an administrator.");
        }

        log.info("Deleting user: {} (Role: {}) by user: {} (Role: {})",
                utilisateur.getIdUtilisateur(), utilisateur.getRole(),
                currentUser.getIdUtilisateur(), currentUser.getRole());

        // Supprimer de Keycloak
        try {
            keycloakService.deleteUser(utilisateur.getKeycloakId());
            log.info("Keycloak user deleted: {}", utilisateur.getKeycloakId());
        } catch (Exception e) {
            log.error("Failed to delete Keycloak user {}: {}", utilisateur.getKeycloakId(), e.getMessage());
            // Still delete from DB even if Keycloak fails
        }

        // Supprimer de la base de données
        utilisateurRepository.delete(utilisateur);

        log.info("Utilisateur supprimé avec succès");
    }

    private UtilisateurResponse mapToResponse(Utilisateur utilisateur) {
        return UtilisateurResponse.builder()
                .idUtilisateur(utilisateur.getIdUtilisateur())
                .keycloakId(utilisateur.getKeycloakId())
                .login(utilisateur.getLogin())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .numTel(utilisateur.getNumTel())
                .signature(utilisateur.getSignature())
                .role(utilisateur.getRole())
                .idCabinet(utilisateur.getIdCabinet()) // NULL pour SUPER_ADMIN et ADMIN
                .actif(utilisateur.getActif())
                .dateCreation(utilisateur.getDateCreation())
                .build();
    }

    /**
     * Mettre à jour les informations d'un utilisateur
     */
    @Transactional
    public UtilisateurResponse updateUtilisateur(Long id, UtilisateurUpdateRequest request) {
        log.info("Mise à jour de l'utilisateur ID: {}", id);

        // ADD AUTHORIZATION CHECK

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        // For MEDECIN updating SECRETAIRE, add extra logging
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String currentUserKeycloakId = jwt.getSubject();
        Utilisateur currentUser = utilisateurRepository.findByKeycloakId(currentUserKeycloakId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        log.info("Update by {} (Role: {}) - Target: {} (Role: {})",
                currentUser.getIdUtilisateur(), currentUser.getRole(),
                utilisateur.getIdUtilisateur(), utilisateur.getRole());

        // MEDECIN can only update certain fields for SECRETAIRE
        if (currentUser.getRole() == Utilisateur.Role.MEDECIN &&
                utilisateur.getRole() == Utilisateur.Role.SECRETAIRE) {
            log.info("MEDECIN updating SECRETAIRE - Allowing name, phone updates");
            // Only allow nom, prenom, numTel updates for MEDECIN managing SECRETAIRE
            // Signature can also be updated if needed
        }

        // Mettre à jour les champs fournis (null = pas de changement)
        if (request.getNom() != null) {
            utilisateur.setNom(request.getNom());
            log.info("Updated nom: {}", request.getNom());
        }
        if (request.getPrenom() != null) {
            utilisateur.setPrenom(request.getPrenom());
            log.info("Updated prenom: {}", request.getPrenom());
        }
        if (request.getNumTel() != null) {
            utilisateur.setNumTel(request.getNumTel());
            log.info("Updated numTel: {}", request.getNumTel());
        }
        if (request.getSignature() != null) {
            utilisateur.setSignature(request.getSignature());
            log.info("Updated signature");
        }

        // Mettre à jour dans Keycloak
        try {
            keycloakService.updateUser(
                    utilisateur.getKeycloakId(),
                    utilisateur.getPrenom(),
                    utilisateur.getNom(),
                    utilisateur.getLogin() + "@cabinet.ma");
            log.info("Keycloak updated for user: {}", utilisateur.getIdUtilisateur());
        } catch (Exception e) {
            log.error("Failed to update Keycloak for user {}: {}", utilisateur.getIdUtilisateur(), e.getMessage());
        }

        utilisateur = utilisateurRepository.save(utilisateur);

        log.info("Utilisateur mis à jour avec succès: {}", utilisateur.getIdUtilisateur());
        return mapToResponse(utilisateur);
    }

    @Transactional
    public UtilisateurResponse updateUserStatus(Long id, Boolean active) {
        log.info("Mise à jour statut utilisateur ID: {} -> {}", id, active);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        utilisateur.setActif(active);

        // Keycloak update (enable/disable user)
        try {
            keycloakService.updateUserStatus(utilisateur.getKeycloakId(), active);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour Keycloak pour l'utilisateur {}: {}", id, e.getMessage());
            // We might want to rollback but for now we log it.
            // Ideally sync should ideally be guaranteed.
        }

        utilisateur = utilisateurRepository.save(utilisateur);
        return mapToResponse(utilisateur);
    }

    /**
     *
     * L'authentification JWT garantit déjà que c'est le bon utilisateur
     */
    @Transactional
    public void changePassword(Long userId, PasswordUpdateRequest request) {
        log.info("Changement de mot de passe pour l'utilisateur ID: {}", userId);

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // L'utilisateur est déjà authentifié via JWT, pas besoin de vérifier l'ancien
        // mot de passe
        // La sécurité est assurée par
        // @PreAuthorize("@utilisateurService.isCurrentUser(#id)")

        keycloakService.updatePassword(utilisateur.getKeycloakId(), request.getNewPassword());

        log.info("Mot de passe changé avec succès pour l'utilisateur: {}", userId);
    }

    /**
     * Réinitialiser le mot de passe d'un utilisateur (par un admin)
     */
    @Transactional
    public void resetPassword(Long userId, AdminPasswordResetRequest request) {
        log.info("Réinitialisation du mot de passe pour l'utilisateur ID: {} (par admin)", userId);

        Utilisateur utilisateur = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour le mot de passe dans Keycloak
        keycloakService.updatePassword(utilisateur.getKeycloakId(), request.getNewPassword());

        log.info("Mot de passe réinitialisé avec succès pour l'utilisateur: {}", userId);
    }

    /**
     * Vérifier si l'utilisateur connecté est celui de l'ID fourni
     */
    public boolean isCurrentUser(Long userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String keycloakId = jwt.getSubject();

            Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
            return utilisateur != null && utilisateur.getKeycloakId().equals(keycloakId);
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public UtilisateurResponse getCurrentUser() {
        log.info("getCurrentUser() called");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        log.info("Keycloak ID from JWT: {}", keycloakId);

        // SIMPLE: Use the regular method
        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> {
                    log.error("User not found in database for Keycloak ID: {}", keycloakId);
                    return new RuntimeException("User not found in database");
                });

        log.info("Found user: {} (ID: {})", utilisateur.getLogin(), utilisateur.getIdUtilisateur());

        return mapToResponse(utilisateur);
    }

    // Add a method to get user WITH signature when explicitly needed
    @Transactional(readOnly = true)
    public UtilisateurResponse getCurrentUserWithSignature() {
        log.info("getCurrentUserWithSignature() called");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        // This will work now because we have @Transactional on the class
        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UtilisateurResponse.builder()
                .idUtilisateur(utilisateur.getIdUtilisateur())
                .keycloakId(utilisateur.getKeycloakId())
                .login(utilisateur.getLogin())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .numTel(utilisateur.getNumTel())
                .signature(utilisateur.getSignature())  // Now safe with @Transactional
                .role(utilisateur.getRole())
                .idCabinet(utilisateur.getIdCabinet())
                .actif(utilisateur.getActif())
                .dateCreation(utilisateur.getDateCreation())
                .build();
    }
}