package com.example.utilisateurservice.service;

import com.example.utilisateurservice.entity.Utilisateur;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.resource}")
    private String clientId;

    private RealmResource realmResource;

    @PostConstruct
    void init() {
        this.realmResource = keycloak.realm(realm);
        log.info("Keycloak admin client ready for realm {}", realm);
    }

    public String createUser(String username, String password, String firstName,
            String lastName, String email, Utilisateur.Role role) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setUsername(username);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setEnabled(true);
            user.setEmailVerified(true);

            Response response = realmResource.users().create(user);

            if (response.getStatus() != 201) {
                throw new RuntimeException("Erreur lors de la création de l'utilisateur dans Keycloak: "
                        + response.getStatusInfo());
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            UserResource userResource = realmResource.users().get(userId);
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            userResource.resetPassword(credential);

            assignRole(userId, role);

            log.info("✅ Utilisateur créé dans Keycloak avec ID: {}", userId);
            return userId;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'utilisateur dans Keycloak", e);
            throw new RuntimeException("Erreur Keycloak: " + e.getMessage());
        }
    }

    public void assignRole(String userId, Utilisateur.Role role) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            RoleRepresentation roleRepresentation = realmResource.roles()
                    .get(role.name()).toRepresentation();
            userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));

            log.info("✅ Rôle {} assigné à l'utilisateur {}", role, userId);
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'assignation du rôle", e);
            throw new RuntimeException("Erreur lors de l'assignation du rôle: " + e.getMessage());
        }
    }

    public void deleteUser(String keycloakId) {
        try {
            realmResource.users().get(keycloakId).remove();
            log.info("✅ Utilisateur supprimé de Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression de l'utilisateur dans Keycloak", e);
            throw new RuntimeException("Erreur Keycloak: " + e.getMessage());
        }
    }

    public void updateUser(String keycloakId, String firstName, String lastName, String email) {
        try {
            UserResource userResource = realmResource.users().get(keycloakId);
            UserRepresentation user = userResource.toRepresentation();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            userResource.update(user);

            log.info("✅ Utilisateur mis à jour dans Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour de l'utilisateur dans Keycloak", e);
            throw new RuntimeException("Erreur Keycloak: " + e.getMessage());
        }
    }

    public void updatePassword(String keycloakId, String newPassword) {
        try {
            UserResource userResource = realmResource.users().get(keycloakId);
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(newPassword);
            credential.setTemporary(false);
            userResource.resetPassword(credential);

            log.info("✅ Mot de passe mis à jour pour l'utilisateur: {}", keycloakId);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour du mot de passe", e);
            throw new RuntimeException("Erreur Keycloak: " + e.getMessage());
        }
    }

    public void updateUserStatus(String keycloakId, boolean enabled) {
        try {
            UserResource userResource = realmResource.users().get(keycloakId);
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(enabled);
            userResource.update(user);

            log.info("✅ Statut utilisateur mis à jour dans Keycloak: {} -> {}", keycloakId, enabled);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la mise à jour du statut dans Keycloak", e);
            throw new RuntimeException("Erreur Keycloak: " + e.getMessage());
        }
    }
}