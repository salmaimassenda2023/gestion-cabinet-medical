package com.example.rendezvousservice.enums;


public enum MotifRendezVous {
    CONSULTATION("Consultation médicale"),
    CONTROLE("Contrôle de suivi"),
    URGENCE("Urgence médicale"),
    AUTRE("Autre motif");

    private final String description;

    MotifRendezVous(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
