-- ============================================
-- V3__create_documents_medicaux_table.sql
-- ============================================
-- Création de la table documents_medicaux

CREATE TABLE documents_medicaux (
                                    id_document BIGSERIAL PRIMARY KEY,
                                    dossier_medical_id BIGINT NOT NULL,
                                    type VARCHAR(50) NOT NULL,
                                    nom VARCHAR(255) NOT NULL,
                                    url VARCHAR(500) NOT NULL,
                                    taille_octets BIGINT,
                                    date_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    CONSTRAINT fk_document_dossier
                                        FOREIGN KEY (dossier_medical_id)
                                            REFERENCES dossiers_medicaux(patient_id)
                                            ON DELETE CASCADE
);

-- Création des index
CREATE INDEX idx_dossier_medical_id ON documents_medicaux(dossier_medical_id);
CREATE INDEX idx_type ON documents_medicaux(type);