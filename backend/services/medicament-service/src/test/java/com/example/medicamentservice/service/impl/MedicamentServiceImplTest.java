package com.example.medicamentservice.service.impl;

import org.example.entity.Medicament;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.example.service.impl.MedicamentServiceImpl;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicamentServiceImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MedicamentServiceImpl medicamentService;

    private Medicament medicament1;
    private Medicament medicament2;

    @BeforeEach
    void setUp() {
        medicament1 = new Medicament();
        medicament1.setId("1");
        medicament1.setNom("Paracétamol");

        medicament2 = new Medicament();
        medicament2.setId("2");
        medicament2.setNom("Ibuprofène");
    }

    @Test
    void findAll_ShouldReturnAllMedicaments() {
        // Arrange
        List<Medicament> expectedMedicaments = Arrays.asList(medicament1, medicament2);
        when(mongoTemplate.findAll(Medicament.class)).thenReturn(expectedMedicaments);

        // Act
        List<Medicament> result = medicamentService.findAll();

        // Assert
        assertEquals(2, result.size());
        assertEquals(expectedMedicaments, result);
        verify(mongoTemplate).findAll(Medicament.class);
    }

    @Test
    void save_ShouldSaveAndReturnMedicament() {
        // Arrange
        when(mongoTemplate.save(medicament1)).thenReturn(medicament1);

        // Act
        Medicament result = medicamentService.save(medicament1);

        // Assert
        assertEquals(medicament1, result);
        verify(mongoTemplate).save(medicament1);
    }

    @Test
    void saveAllMedicaments_ShouldSaveAllMedicaments() {
        // Arrange
        List<Medicament> medicaments = Arrays.asList(medicament1, medicament2);
        when(mongoTemplate.save(medicament1)).thenReturn(medicament1);
        when(mongoTemplate.save(medicament2)).thenReturn(medicament2);

        // Act
        List<Medicament> result = medicamentService.saveAllMedicaments(medicaments);

        // Assert
        assertEquals(2, result.size());
        verify(mongoTemplate, times(2)).save(any(Medicament.class));
    }

    @Test
    void searchByNomStartingWith_ShouldReturnMatchingMedicaments() {
        // Arrange
        String prefix = "Para";
        List<Medicament> expectedMedicaments = Arrays.asList(medicament1);

        Query expectedQuery = new Query(Criteria.where("nom")
                .regex(Pattern.compile("^" + Pattern.quote(prefix), Pattern.CASE_INSENSITIVE)));
        expectedQuery.limit(10);
        expectedQuery.with(Sort.by(Sort.Direction.ASC, "nom"));

        when(mongoTemplate.find(eq(expectedQuery), eq(Medicament.class)))
                .thenReturn(expectedMedicaments);

        // Act
        List<Medicament> result = medicamentService.searchByNomStartingWith(prefix);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Paracétamol", result.get(0).getNom());
        verify(mongoTemplate).find(eq(expectedQuery), eq(Medicament.class));
    }

    @Test
    void searchByNomContaining_ShouldReturnMatchingMedicaments() {
        // Arrange
        String keyword = "prof";
        List<Medicament> expectedMedicaments = Arrays.asList(medicament2);

        Query expectedQuery = new Query(Criteria.where("nom")
                .regex(Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE)));
        expectedQuery.limit(20);
        expectedQuery.with(Sort.by(Sort.Direction.ASC, "nom"));

        when(mongoTemplate.find(eq(expectedQuery), eq(Medicament.class)))
                .thenReturn(expectedMedicaments);

        // Act
        List<Medicament> result = medicamentService.searchByNomContaining(keyword);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Ibuprofène", result.get(0).getNom());
        verify(mongoTemplate).find(eq(expectedQuery), eq(Medicament.class));
    }

    @Test
    void searchMedicaments_ShouldReturnMatchingMedicaments() {
        // Arrange
        String searchTerm = "para";
        List<Medicament> expectedMedicaments = Arrays.asList(medicament1);

        Query expectedQuery = new Query(Criteria.where("nom")
                .regex(Pattern.quote(searchTerm), "i"));
        expectedQuery.limit(15);
        expectedQuery.with(Sort.by(Sort.Direction.ASC, "nom"));

        when(mongoTemplate.find(eq(expectedQuery), eq(Medicament.class)))
                .thenReturn(expectedMedicaments);

        // Act
        List<Medicament> result = medicamentService.searchMedicaments(searchTerm);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Paracétamol", result.get(0).getNom());
        verify(mongoTemplate).find(eq(expectedQuery), eq(Medicament.class));
    }

    @Test
    void searchByNomStartingWith_EmptyPrefix_ShouldReturnEmptyList() {
        // Arrange
        when(mongoTemplate.find(any(Query.class), eq(Medicament.class)))
                .thenReturn(List.of());

        // Act
        List<Medicament> result = medicamentService.searchByNomStartingWith("");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void saveAllMedicaments_EmptyList_ShouldReturnEmptyList() {
        // Act
        List<Medicament> result = medicamentService.saveAllMedicaments(List.of());

        // Assert
        assertTrue(result.isEmpty());
        verify(mongoTemplate, never()).save(any(Medicament.class));
    }
}