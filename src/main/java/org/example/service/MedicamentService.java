package org.example.service;

import org.example.entity.Medicament;

import java.util.List;

public interface MedicamentService {

    public List<Medicament> findAll();

    public Medicament save(Medicament medicament);

    public List<Medicament> saveAllMedicaments(List<Medicament> medicaments);

    public List<Medicament> searchByNomStartingWith(String prefix);

    public List<Medicament> searchByNomContaining(String keyword);

    public List<Medicament> searchMedicaments(String searchTerm);

}
