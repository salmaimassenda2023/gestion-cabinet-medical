package org.example.service.impl;

import org.example.entity.Medicament;
import org.example.service.MedicamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class MedicamentServiceImpl implements MedicamentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<Medicament> findAll() {
        return mongoTemplate.findAll(Medicament.class);
    }

    public Medicament save(Medicament medicament) {
        return mongoTemplate.save(medicament);
    }

    public List<Medicament> saveAllMedicaments(List<Medicament> medicaments) {
        return medicaments.stream()
                .map(this::save)
                .toList();
    }

    // Autocompletion methods
    public List<Medicament> searchByNomStartingWith(String prefix) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(prefix), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("nom").regex(pattern));
        query.limit(10); // Limit results for autocomplete
        query.with(Sort.by(Sort.Direction.ASC, "nom"));
        return mongoTemplate.find(query, Medicament.class);
    }

    public List<Medicament> searchByNomContaining(String keyword) {
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("nom").regex(pattern));
        query.limit(20);
        query.with(Sort.by(Sort.Direction.ASC, "nom"));
        return mongoTemplate.find(query, Medicament.class);
    }

    public List<Medicament> searchMedicaments(String searchTerm) {
        // Search in nom field
        Criteria criteria = Criteria.where("nom").regex(Pattern.quote(searchTerm), "i");
        Query query = new Query(criteria);
        query.limit(15);
        query.with(Sort.by(Sort.Direction.ASC, "nom"));
        return mongoTemplate.find(query, Medicament.class);
    }
}