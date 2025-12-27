package org.example.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "medicaments")
public class Medicament {

    @Id
    private String id;

    @Indexed
    private String nom;

    private String dosage;

    private String forme;

    public Medicament() {
    }

    public Medicament(String nom, String dosage, String forme) {
        this.nom = nom;
        this.dosage = dosage;
        this.forme = forme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getForme() {
        return forme;
    }

    public void setForme(String forme) {
        this.forme = forme;
    }

    @Override
    public String toString() {
        return "Medicament{" +
                "id='" + id + '\'' +
                ", nom='" + nom + '\'' +
                ", dosage='" + dosage + '\'' +
                ", forme='" + forme + '\'' +
                '}';
    }
}