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

import java.util.List;
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

        // ✅ VALIDATION 1 : idCabinet obligatoire UNIQUEMENT pour MEDECIN et SECRETAIRE
        if ((request.getRole() == Utilisateur.Role.MEDECIN || request.getRole() == Utilisateur.Role.SECRETAIRE)
                && request.getIdCabinet() == null) {
            throw new IllegalArgumentException(
                    "Le champ idCabinet est obligatoire pour les rôles MEDECIN et SECRETAIRE");
        }

        // ✅ VALIDATION 2 : SUPER_ADMIN et ADMIN ne doivent PAS avoir de cabinet
        if ((request.getRole() == Utilisateur.Role.SUPER_ADMIN || request.getRole() == Utilisateur.Role.ADMIN)
                && request.getIdCabinet() != null) {
            log.warn("⚠️ idCabinet ignoré pour {} (valeur reçue: {})", request.getRole(), request.getIdCabinet());
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

        log.info("✅ Médecin inscrit avec succès: {}", utilisateur.getIdUtilisateur());
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

    public UtilisateurResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String keycloakId = jwt.getSubject();

        Utilisateur utilisateur = utilisateurRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté non trouvé"));

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

    public List<UtilisateurResponse> getUtilisateursByCabinet(Long idCabinet) {
        log.info("Récupération des utilisateurs du cabinet: {}", idCabinet);
        return utilisateurRepository.findByIdCabinet(idCabinet).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUtilisateur(Long id) {
        log.info("Suppression de l'utilisateur avec l'ID: {}", id);

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Supprimer de Keycloak
        keycloakService.deleteUser(utilisateur.getKeycloakId());

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

        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec l'ID: " + id));

        // Mettre à jour les champs fournis (null = pas de changement)
        if (request.getNom() != null) {
            utilisateur.setNom(request.getNom());
        }
        if (request.getPrenom() != null) {
            utilisateur.setPrenom(request.getPrenom());
        }
        if (request.getNumTel() != null) {
            utilisateur.setNumTel(request.getNumTel());
        }
        if (request.getSignature() != null) {
            utilisateur.setSignature(request.getSignature());
        }

        // Mettre à jour dans Keycloak
        keycloakService.updateUser(
                utilisateur.getKeycloakId(),
                utilisateur.getPrenom(),
                utilisateur.getNom(),
                utilisateur.getLogin() + "@cabinet.ma");

        utilisateur = utilisateurRepository.save(utilisateur);

        log.info("✅ Utilisateur mis à jour avec succès: {}", utilisateur.getIdUtilisateur());
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

        log.info("✅ Mot de passe changé avec succès pour l'utilisateur: {}", userId);
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

        log.info("✅ Mot de passe réinitialisé avec succès pour l'utilisateur: {}", userId);
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
}