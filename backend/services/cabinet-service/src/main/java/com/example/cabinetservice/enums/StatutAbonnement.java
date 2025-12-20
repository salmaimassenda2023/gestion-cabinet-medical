package com.example.cabinetservice.enums;

/**
 * Enum - Statut Abonnement
 */
public enum StatutAbonnement {
    ACTIF("Actif"),
    EXPIRE("Expiré"),
    SUSPENDU("Suspendu");

    private final String libelle;

    StatutAbonnement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
