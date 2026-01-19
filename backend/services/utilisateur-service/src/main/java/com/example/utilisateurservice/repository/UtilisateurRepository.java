package com.example.utilisateurservice.repository;


import com.example.utilisateurservice.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByKeycloakId(String keycloakId);
    Optional<Utilisateur> findByLogin(String login);
    List<Utilisateur> findByIdCabinet(Long idCabinet);
    List<Utilisateur> findByRole(Utilisateur.Role role);
    boolean existsByLogin(String login);

}

