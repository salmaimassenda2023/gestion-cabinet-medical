-- ============================================
-- V2__create_dossiers_medicaux_table.sql
-- ============================================
-- Création de la table dossiers_medicaux avec Shared Primary Key

CREATE TABLE dossiers_medicaux (
                                   patient_id BIGINT PRIMARY KEY,
                                   antecedents_medicaux TEXT,
                                   antecedents_chirurgicaux TEXT,
                                   allergies TEXT,
                                   groupe_sanguin VARCHAR(5),
                                   remarques TEXT,
                                   date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_dossier_patient
                                       FOREIGN KEY (patient_id)
                                           REFERENCES patients(id)
                                           ON DELETE CASCADE
);