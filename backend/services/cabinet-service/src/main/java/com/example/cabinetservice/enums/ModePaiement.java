package com.example.cabinetservice.enums;

public enum ModePaiement {
    CARTE("Carte bancaire"),
    VIREMENT("Virement bancaire"),
    ESPECES("Espèces"),
    CHEQUE("Chèque");

    private final String libelle;

    ModePaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
