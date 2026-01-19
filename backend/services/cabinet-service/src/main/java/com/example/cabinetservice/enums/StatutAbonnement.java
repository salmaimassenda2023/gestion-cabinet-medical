package com.example.cabinetservice.enums;

public enum StatutAbonnement {
    ACTIF("Actif"),
    EXPIRE("Expiré");

    private final String libelle;

    StatutAbonnement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
