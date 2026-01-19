package com.example.cabinetservice.enums;


public enum StatutPaiement {
    EN_ATTENTE("En attente"),
    VALIDE("Validé"),
    REFUSE("Refusé") ;

    private final String libelle;

    StatutPaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
