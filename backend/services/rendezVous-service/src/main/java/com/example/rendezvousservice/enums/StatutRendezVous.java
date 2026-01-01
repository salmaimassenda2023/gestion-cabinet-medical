package com.example.rendezvousservice.enums;

public enum StatutRendezVous {
    PLANIFIE("Planifié"),
    CONFIRME("Confirmé"),
    PRESENT("Patient présent"),
    EN_CONSULTATION("En consultation"),
    TERMINE("Terminé"),
    ANNULE("Annulé");

    private final String description;

    StatutRendezVous(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
