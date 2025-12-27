package org.example.csv;

import org.example.entity.Medicament;
import org.example.service.MedicamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvLoaderService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MedicamentService medicamentService;

    @EventListener(ApplicationReadyEvent.class)
    public void loadCsvData() {
        try {
            System.out.println("Starting CSV data load...");

            // Check if data already exists
            long count = mongoTemplate.getCollection("medicaments").countDocuments();
            if (count > 0) {
                System.out.println("Data already loaded. Skipping CSV import.");
                return;
            }

            // Load CSV file from resources
            ClassPathResource resource = new ClassPathResource("medicaments.csv");
            if (!resource.exists()) {
                System.err.println("CSV file not found in resources!");
                return;
            }

            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            String content = FileCopyUtils.copyToString(reader);
            reader.close();

            // Debug: Show file content
            System.out.println("File loaded successfully. Length: " + content.length() + " chars");
            System.out.println("First 500 chars: " + content.substring(0, Math.min(500, content.length())));

            // Parse CSV
            List<Medicament> medicaments = parseCsv(content);

            // Save to MongoDB
            if (!medicaments.isEmpty()) {
                medicamentService.saveAllMedicaments(medicaments);
                System.out.println("Successfully loaded " + medicaments.size() + " medicaments from CSV");

                // Verify by counting
                long newCount = mongoTemplate.getCollection("medicaments").countDocuments();
                System.out.println("Total medicaments in DB: " + newCount);
            } else {
                System.out.println("No medicaments found in CSV file");
                // Debug why parsing failed
                debugParse(content);
            }

        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Medicament> parseCsv(String csvContent) {
        List<Medicament> medicaments = new ArrayList<>();
        String[] lines = csvContent.split("\\r?\\n"); // Handles both Windows and Unix line endings

        System.out.println("Total lines in CSV: " + lines.length);

        // Skip header (first line)
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                String[] columns = parseCsvLine(line);
                System.out.println("Line " + i + " parsed into " + columns.length + " columns: " + line);

                if (columns.length >= 3) {
                    try {
                        Medicament medicament = new Medicament(
                                columns[0].trim(), // NOM
                                columns[1].trim(), // DOSAGE1
                                columns[2].trim()  // FORME
                        );
                        medicaments.add(medicament);
                        System.out.println("Added: " + medicament.getNom());
                    } catch (Exception e) {
                        System.err.println("Error creating medicament from line " + i + ": " + e.getMessage());
                    }
                } else {
                    System.err.println("Line " + i + " has only " + columns.length + " columns. Expected at least 3.");
                    System.err.println("Line content: " + line);
                }
            }
        }
        return medicaments;
    }

    private String[] parseCsvLine(String line) {
        List<String> columns = new ArrayList<>();
        StringBuilder currentColumn = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ';' && !inQuotes) {
                columns.add(currentColumn.toString());
                currentColumn = new StringBuilder();
            } else {
                currentColumn.append(c);
            }
        }
        columns.add(currentColumn.toString());

        return columns.toArray(new String[0]);
    }

    private void debugParse(String csvContent) {
        System.out.println("\n=== DEBUG PARSING ===");
        String[] lines = csvContent.split("\\r?\\n");

        for (int i = 0; i < Math.min(10, lines.length); i++) {
            System.out.println("Line " + i + ": \"" + lines[i] + "\"");
            if (i == 0) {
                System.out.println("Header fields: " + lines[i].replace(";", " | "));
            }
        }

        // Try manual parsing of first data line
        if (lines.length > 1) {
            String firstDataLine = lines[1];
            String[] manualSplit = firstDataLine.split(";");
            System.out.println("\nManual split of line 1:");
            for (int j = 0; j < manualSplit.length; j++) {
                System.out.println("  Column " + j + ": \"" + manualSplit[j] + "\"");
            }
        }
    }
}